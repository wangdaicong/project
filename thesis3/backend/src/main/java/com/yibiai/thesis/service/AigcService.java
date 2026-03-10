package com.yibiai.thesis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AigcService {

    private final DeepSeekService deepSeekService;

    public AigcService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    public Flux<String> reduceAigc(String content, String language) {
        List<String> segments = splitTextIntoSegments(content, 500);
        AtomicReference<List<DeepSeekService.Message>> historyRef = new AtomicReference<>(List.of());

        return Flux.fromIterable(segments)
                .concatMap(segment -> processSegment(segment, historyRef))
                .onErrorResume(e -> Flux.just("\n\n处理失败: " + e.getMessage()));
    }

    private Flux<String> processSegment(String segment, AtomicReference<List<DeepSeekService.Message>> historyRef) {
        if (segment == null || segment.isBlank()) {
            return Flux.empty();
        }

        if (countTextLength(segment) < 20) {
            return Flux.just(segment + "\n\n");
        }

        Mono<String> polishedMono = callPolish(segment, historyRef.get()).collectList().map(list -> String.join("", list));

        return polishedMono.flatMapMany(polished ->
                callEnhance(polished, historyRef.get())
                        .collectList()
                        .flatMapMany(chunks -> {
                            String enhanced = String.join("", chunks);
                            historyRef.set(nextHistory(historyRef.get(), enhanced));
                            return Flux.fromIterable(chunks)
                                    .concatWithValues("\n\n");
                        })
        );
    }

    private Flux<String> callPolish(String text, List<DeepSeekService.Message> history) {
        List<DeepSeekService.Message> messages = new ArrayList<>(history);
        messages.add(new DeepSeekService.Message("system", DEFAULT_POLISH_PROMPT));
        messages.add(new DeepSeekService.Message("user", text));
        return deepSeekService.chatStream(messages);
    }

    private Flux<String> callEnhance(String text, List<DeepSeekService.Message> history) {
        List<DeepSeekService.Message> messages = new ArrayList<>(history);
        messages.add(new DeepSeekService.Message("system", DEFAULT_ENHANCE_PROMPT));
        messages.add(new DeepSeekService.Message("user", text));
        return deepSeekService.chatStream(messages);
    }

    private List<DeepSeekService.Message> nextHistory(List<DeepSeekService.Message> current, String assistantText) {
        List<DeepSeekService.Message> next = new ArrayList<>();
        if (current != null) {
            for (DeepSeekService.Message m : current) {
                if (m != null && "assistant".equals(m.role()) && m.content() != null && !m.content().isBlank()) {
                    next.add(m);
                }
            }
        }
        next.add(new DeepSeekService.Message("assistant", assistantText));
        int maxKeep = 3;
        if (next.size() <= maxKeep) {
            return next;
        }
        return next.subList(next.size() - maxKeep, next.size());
    }

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff]");
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]");

    private int countTextLength(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int chineseCount = 0;
        var m = CHINESE_PATTERN.matcher(text);
        while (m.find()) {
            chineseCount++;
        }
        if (chineseCount > 0) {
            return chineseCount;
        }
        int enCount = 0;
        var em = ENGLISH_PATTERN.matcher(text);
        while (em.find()) {
            enCount++;
        }
        return enCount;
    }

    private List<String> splitTextIntoSegments(String text, int maxChars) {
        List<String> segments = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return segments;
        }

        String[] paragraphs = text.split("\\n");
        for (String para : paragraphs) {
            if (para == null) {
                continue;
            }
            String p = para.trim();
            if (p.isEmpty()) {
                continue;
            }
            if (countTextLength(p) <= maxChars) {
                segments.add(p);
                continue;
            }

            List<String> parts = splitLongParagraph(p, maxChars);
            segments.addAll(parts);
        }
        return segments;
    }

    private List<String> splitLongParagraph(String paragraph, int maxChars) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        String[] tokens = paragraph.split("(?<=[。！？!?;；])");
        for (String token : tokens) {
            String t = token == null ? "" : token;
            if (t.isEmpty()) {
                continue;
            }
            if (countTextLength(cur + t) <= maxChars) {
                cur.append(t);
            } else {
                if (!cur.isEmpty()) {
                    out.add(cur.toString());
                }
                cur.setLength(0);
                cur.append(t);
            }
        }
        if (!cur.isEmpty()) {
            out.add(cur.toString());
        }
        return out;
    }

     public static final String DEFAULT_POLISH_PROMPT = "你是中国某高校的硕士研究生，写作水平中等偏上。" +
            "现在你要用自己的话重写下面这段论文内容，不是逐词替换，是真正用你自己的表达习惯重新写一遍。" +
            "要求：" +
            "(1)用你自己的话重新组织语言，有些地方写得细一点，有些地方概括一下就行，不用每句话都写得一样精细；" +
            "(2)衔接不用太刻意，有些句子之间直接接上去就行，不用非得加'然而''此外''因此'这种连接词；" +
            "(3)绝对不要用这些词：'值得注意的是''综上所述''毋庸置疑''不言而喻''众所周知''至关重要''不可或缺''日益增长'；" +
            "(4)句子有长有短，有的句子可以写得随意一点，不用每句都很工整，故意让有些句子看起来不那么'完美'；" +
            "(5)相邻两个句子绝对不能用相同的句式结构，如果前一句是'A是B'，下一句就不能也是'X是Y'，要换成完全不同的结构；" +
            "(6)偶尔可以用一些不太正式但学术论文中也能接受的表达，比如'说白了就是''简单来讲''大致来看'；" +
            "(7)有些句子可以故意写得啰嗦一点，有些句子又写得很简短，让整段的信息密度忽高忽低；" +
            "(8)术语、数据、公式、引用编号[1][2]等保持不变；" +
            "(9)字数和原文差不多，不要多也不要少。" +
            "直接输出改写后的文字，不要加任何解释或标注。";

     public static final String DEFAULT_ENHANCE_PROMPT = "你是同一个硕士生，导师觉得上一版还是有点像AI写的，让你再改一轮。这次你要：" +
            "(1)把太'顺滑工整'的句子改得粗糙一些，像是真人赶deadline写出来的而不是机器精心生成的；" +
            "(2)句子长短变化要更极端，有的句子就七八个字，有的句子写到三四十个字，不要均匀；" +
            "(3)调整表达方式，用更朴素的说法，比如'取得了较好的效果'改成'效果还不错'，'进行了深入分析'改成'分析了一下'；" +
            "(4)多加一些'大致''基本上''从目前来看''差不多''初步来看'这种带犹豫感的表达；" +
            "(5)不要所有句子都是同一种结构，每两个相邻句子的语法结构必须不同；" +
            "(6)偶尔可以把一个完整的长句拆成两个短句，也可以把两个短句合成一个长句；" +
            "(7)有的地方可以故意写得不那么严谨，比如'在很大程度上'可以写成'多半是因为'；" +
            "(8)术语数据引用编号不动，字数和上一版差不多。" +
            "直接输出改写后的文字，不要加任何解释或标注。";

     public static final String DEAI_PROMPT = "你现在扮演一个凌晨三点还在赶论文的硕士生。你已经很困了，但必须把这段文字改到看起来不像AI写的。" +
            "你的改写原则：" +
            "(1)每个句子都必须重新组织语序。原文说'A导致了B的提升'，你要改成'B提升了，跟A有关'或'B方面有所好转，A起了作用'这种完全不同的说法；" +
            "(2)删掉所有华丽的修饰词。'显著提升'改成'提升了一些'，'有效解决'改成'解决了'，'深入探讨'改成'讨论了'，'全面分析'改成'分析了'；" +
            "(3)句子长度必须极度不均匀：有的句子只有五六个字就结束，有的句子写到四五十个字才打句号，中间不要有规律，像真人思维跳跃那样随意切换；" +
            "(4)有些句子故意写得罗嗦一点，绕一下弯再说到点子上，像真人说话那样不那么直接；" +
            "(5)选词要避开第一反应的那个词：比如不说'提高'而说'拉高'，不说'影响'而说'波及'，不说'优化'而说'调整'，不说'方法'而说'办法'或'路子'，不说'实现'而说'做到'——用朴素的、不太书面的词；" +
            "(6)绝对禁止用这些表达：'值得注意的是''综上所述''此外''然而''因此''进而''旨在''呈现''揭示''致力于''日益''逐步''广泛''充分''深入''有效地'；" +
            "(7)把'进行了XX分析/研究/探讨'全部改成'分析了/研究了/讨论了'这种简单动宾结构；" +
            "(8)每个句子用不同的语气和节奏，前一句用陈述语气，后一句可以用推测语气'大概''可能'，再后一句换成肯定的短句，不要让相邻句子读起来像同一个人同一口气写的；" +
            "(9)术语、数据、公式、引用编号保持不变，字数和原文差不多。" +
            "直接输出改写后的文字。";
}
