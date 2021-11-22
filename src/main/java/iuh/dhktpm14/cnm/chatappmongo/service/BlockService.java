package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Block;
import iuh.dhktpm14.cnm.chatappmongo.repository.BlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BlockService {
    @Autowired
    private BlockRepository blockRepository;

    public Page<Block> findAllByUserId(String userId, Pageable pageable) {
        return blockRepository.findAllByUserId(userId, pageable);
    }

    public boolean checkMeBlockThisUser(String currentUserId, String anotherUserId) {
        return blockRepository.checkMeBlockThisUser(currentUserId, anotherUserId);
    }

    public boolean checkThisUserBlockMe(String currentUserId, String anotherUserId) {
        return blockRepository.checkThisUserBlockMe(currentUserId, anotherUserId);
    }

    public long unBlock(String currentUserId, String anotherUserId) {
        return blockRepository.unBlock(currentUserId, anotherUserId);
    }

    public Optional<Block> findById(String id) {
        return blockRepository.findById(id);
    }

    public Optional<Block> findByUserIdAndBlockUserId(String currentUserId, String anotherUserId) {
        return blockRepository.findByUserIdAndBlockUserId(currentUserId, anotherUserId);
    }

    public Block save(Block block) {
        return blockRepository.save(block);
    }

}
