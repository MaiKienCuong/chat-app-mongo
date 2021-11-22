package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.BlockDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Block;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.mapper.BlockMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/blocks")
@CrossOrigin("${spring.security.cross_origin}")
public class BlockRest {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockMapper blockMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AppUserDetailService userDetailService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách những người mà mình đã chặn")
    public ResponseEntity<?> getAllBlockListPaging(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        log.info("getting block list of userId = {}, page = {}, size = {}", user.getId(), pageable.getPageNumber(), pageable.getPageSize());
        Page<Block> blockPage = blockService.findAllByUserId(user.getId(), pageable);
        return ResponseEntity.ok(toBlockDto(blockPage));
    }

    @PostMapping("/{idOfUserToBlock}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chặn một người")
    public ResponseEntity<?> blockUser(@ApiIgnore @AuthenticationPrincipal User user,
                                       @PathVariable String idOfUserToBlock,
                                       Locale locale) {
        log.info("userId = {} is blocking userId = {}", user.getId(), idOfUserToBlock);
        if (user.getId().equals(idOfUserToBlock)) {
            log.error("cannot block to myself");
            return ResponseEntity.badRequest().build();
        }
        if (userDetailService.existsById(idOfUserToBlock)) {
            if (! blockService.checkMeBlockThisUser(user.getId(), idOfUserToBlock)) {
                var block = Block.builder()
                        .userId(user.getId())
                        .blockId(idOfUserToBlock)
                        .createAt(new Date())
                        .build();
                blockService.save(block);
                log.info("userId = {} is block userId = {} successfully", user.getId(), idOfUserToBlock);
                return ResponseEntity.ok(blockMapper.toBlockDto(block));
            } else {
                String message = messageSource.getMessage("block_is_already_in_database",
                        new Object[]{ user.getId(), idOfUserToBlock }, locale);
                log.error(message);
                return ResponseEntity.badRequest().build();
            }
        }
        String message = messageSource.getMessage("user_not_found",
                new Object[]{ user.getId(), idOfUserToBlock }, locale);
        log.error(message);
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{idOfUserToUnBlock}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Bỏ chặn một người")
    public ResponseEntity<?> unBlockUser(@ApiIgnore @AuthenticationPrincipal User user,
                                         @PathVariable String idOfUserToUnBlock) {
        long deletedCount = blockService.unBlock(user.getId(), idOfUserToUnBlock);
        var message = String.format("%d %s", deletedCount, "item deleted");
        return ResponseEntity.ok(new MessageResponse(message));
    }

    private Page<?> toBlockDto(Page<Block> blockPage) {
        List<Block> content = blockPage.getContent();
        List<BlockDto> dto = content.stream()
                .map(x -> blockMapper.toBlockDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, blockPage.getPageable(), blockPage.getTotalElements());
    }

}
