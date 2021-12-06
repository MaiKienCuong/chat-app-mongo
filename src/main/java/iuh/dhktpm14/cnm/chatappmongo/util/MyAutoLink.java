package iuh.dhktpm14.cnm.chatappmongo.util;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.nibor.autolink.Span;
import org.owasp.encoder.Encode;

import java.util.EnumSet;

public class MyAutoLink {

    private MyAutoLink() {
    }

    private static final LinkExtractor linkExtractor = LinkExtractor.builder()
            .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW, LinkType.EMAIL))
            .build();

    public static String detectLink(String input) {
        Iterable<Span> spans = linkExtractor.extractSpans(input);

        var sb = new StringBuilder();
        for (Span span : spans) {
            var text = input.substring(span.getBeginIndex(), span.getEndIndex());
            if (span instanceof LinkSpan && ((LinkSpan) span).getType().equals(LinkType.EMAIL)) {
                sb.append("<a href=\"mailto:");
                sb.append(Encode.forHtmlAttribute(text));
                sb.append("\">");
                sb.append(Encode.forHtml(text));
                sb.append("</a>");
            } else if (span instanceof LinkSpan) {
                if (((LinkSpan) span).getType().equals(LinkType.WWW)
                        || ((LinkSpan) span).getType().equals(LinkType.URL)) {
                    sb.append("<a href=\"");
                    sb.append(Encode.forHtmlAttribute(text));
                    sb.append("\">");
                    sb.append(Encode.forHtml(text));
                    sb.append("</a>");
                }
            } else {
                sb.append(Encode.forHtml(text));
            }
        }

        return sb.toString();
    }
}
