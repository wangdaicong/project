package com.yibiai.thesis.service;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.*;

/**
 * 困惑度注入引擎（Anti-AIGC Engine）
 * 核心原理：AIGC检测器依赖困惑度(perplexity)和突发性(burstiness)
 * - AI文本：困惑度低且均匀（每个词都是"最可能的下一个词"）
 * - 人类文本：困惑度高且波动大（词汇选择不可预测，句子复杂度波动）
 * 本引擎通过以下手段提高困惑度和突发性：
 * 1. 在高概率n-gram中间插入修饰语（打断"最可能的下一个词"）
 * 2. 句子复杂度极端波动（超短句和超长句交替）
 * 3. 插入括号补充、破折号解释（人类写作的"打断-补充"模式）
 * 4. 非常规句式结构（定语后置、状语前移）
 * 5. 同义词替换（改变词汇分布）
 */
@Component
public class RuleBasedRewriter {
    
    private static final Random RANDOM = new Random();
    
    // ========== 同义词库 ==========
    private static final String[][] SYNONYM_DICT = {
            // 连接词（AI最典型特征）
            {"然而，", "不过，|但，|可，|话说回来，"},
            {"然而", "不过|但|可|话说回来"},
            {"此外，", "另外，|还有，|再者，|除此之外，"},
            {"此外", "另外|还有|再者|除此之外"},
            {"因此，", "所以，|这样一来，|由此，|也就是说，"},
            {"因此", "所以|这样一来|由此|也就是说"},
            {"同时，", "而且，|并且，|加上，|与此同步，"},
            {"同时", "而且|并且|加上|与此同步"},
            {"首先，", "第一，|先看，|一方面，"},
            {"首先", "第一|先看|一方面"},
            {"其次，", "第二，|再看，|接着，|然后，"},
            {"其次", "第二|再看|接着|然后"},
            {"最后，", "最终，|末了，|收尾来看，"},
            {"最后", "最终|末了|收尾来看"},
            {"总之，", "总的来说，|归根结底，|说到底，"},
            {"总之", "总的来说|归根结底|说到底"},
            {"综上所述，", "综合前面的分析，|汇总来看，|整体来看，"},
            {"综上所述", "综合前面的分析|汇总来看|整体来看"},
            {"由此可见，", "从这里能看出，|据此判断，|这说明，"},
            {"由此可见", "从这里能看出|据此判断|这说明"},
            {"值得注意的是，", "需要留意的一点是，|这里有个关键点，"},
            {"值得注意的是", "需要留意的一点是|这里有个关键点"},
            {"需要指出的是，", "要说明一下，|这里补充一点，"},
            {"需要指出的是", "要说明一下|这里补充一点"},
            {"具体而言，", "具体来说，|展开讲，|落到实处，"},
            {"具体而言", "具体来说|展开讲|落到实处"},
            {"在此基础上，", "基于此，|依托这个条件，|沿着这条线，"},
            {"在此基础上", "基于此|依托这个条件|沿着这条线"},
            {"与此同时，", "同一时间，|这过程中，|伴随着，"},
            {"与此同时", "同一时间|这过程中|伴随着"},
            {"不仅如此，", "不光这样，|远不止于此，|除此之外，"},
            {"不仅如此", "不光这样|远不止于此|除此之外"},
            {"换言之，", "说白了，|换句话说，|通俗讲，"},
            {"换言之", "说白了|换句话说|通俗讲"},
            {"事实上，", "实际上，|其实，|说实话，"},
            {"事实上", "实际上|其实|说实话"},
            
            // 动词短语
            {"进行了分析", "分析了|做了分析|加以分析"},
            {"进行了研究", "研究了|做了研究|着手研究"},
            {"进行了探讨", "探讨了|讨论了|加以讨论"},
            {"进行了验证", "验证了|做了验证|经过验证"},
            {"进行了实验", "做了实验|开展实验|设计实验"},
            {"进行了对比", "对比了|做了比较|加以比较"},
            {"进行了优化", "优化了|做了优化|改进了"},
            {"进行了处理", "处理了|做了处理|加以处理"},
            {"进行", "做|开展|实施|执行"},
            
            // 绝对化→不确定
            {"显著提高了", "提高了不少|有所提高|在一定程度上提高了"},
            {"显著提高", "有所提高|提高了不少|在一定程度上提高"},
            {"显著降低了", "降低了一些|有所降低|在一定程度上降低了"},
            {"显著降低", "有所降低|降低了一些|在一定程度上降低"},
            {"显著提升", "有所提升|得到改善|在一定程度上提升"},
            {"显著改善", "有所改善|改善了不少|得到改善"},
            {"显著", "明显|比较突出|相当"},
            {"极大地", "很大程度上|相当程度|较大程度"},
            {"至关重要", "很关键|比较重要|相当重要"},
            {"不可或缺", "很重要|比较重要|难以缺少"},
            {"极其", "很|相当|比较"},
            {"非常", "很|挺|比较|相当"},
            {"十分", "很|相当|挺|比较"},
            
            // 学术套话→朴素
            {"具有重要意义", "有一定意义|意义比较大|比较有意义"},
            {"具有重要的", "有比较重要的|包含关键的|有着重要的"},
            {"具有", "有|包含|带有|含有"},
            {"发挥着重要作用", "起了不小作用|有相当影响|作用比较大"},
            {"呈现出", "表现出|展示了|显示出"},
            {"旨在", "目的在于|是为了|着眼于"},
            {"表明", "说明|显示|反映|证明"},
            {"指出", "提到|说|提及"},
            {"认为", "觉得|看来|判断"},
            {"体现了", "反映了|展现了|表现了"},
            {"揭示了", "反映出|显示出|说明了"},
            {"有效地", "较好地|比较有效地|相对有效"},
            {"充分", "较为充分|比较全面|相对充分"},
            {"日益", "越来越|不断|逐渐"},
            {"逐步", "慢慢|渐渐|一步步"},
            {"广泛", "较为普遍|比较多|大范围"},
            {"深入", "进一步|更细致|更深层"},
            {"全面", "比较全面|较为完整|相对全面"},
            
            // 短语级替换
            {"通过分析", "分析后|经过分析"},
            {"通过研究", "研究后|经过研究"},
            {"通过实验", "实验后|经过实验"},
            {"结果表明", "结果显示|数据显示"},
            {"研究表明", "研究显示|研究发现"},
            {"可以看出", "能看出|看得出|可见"},
            {"可以发现", "能发现|发现|可见"},
            {"能够实现", "可以实现|能实现|做到了"},
            {"有助于", "有利于|帮助|促进"},
            {"有利于", "有助于|帮助|促进"},
            {"促进了", "推动了|帮助了|加快了"},
            {"推动了", "促进了|帮助了|加快了"},
    };
    
    // 需要删除的AI冗余修饰
    private static final String[] REMOVE_PHRASES = {
            "众所周知，", "不言而喻，", "毋庸置疑，",
            "无可否认，", "不可置否，", "毫无疑问地，",
            "可以明确的是，", "显而易见地，", "不容置疑地，",
            "众所周知", "不言而喻", "毋庸置疑",
            "无可否认", "显而易见"
    };
    
    // ========== 在n-gram中间插入的修饰语（提高困惑度的核心） ==========
    // 格式：{原始n-gram, 插入修饰语后的版本1|版本2|...}
    private static final String[][] NGRAM_BREAKERS = {
            // 打断"的+名词"高概率搭配
            {"的研究", "的相关研究|的这项研究|的已有研究|方面的研究"},
            {"的分析", "的具体分析|的这项分析|的初步分析|方面的分析"},
            {"的方法", "的一种方法|的具体方法|的可行方法|层面的方法"},
            {"的模型", "的这一模型|的一种模型|的所用模型|层面的模型"},
            {"的数据", "的相关数据|的实际数据|的已有数据|方面的数据"},
            {"的结果", "的实验结果|的具体结果|的最终结果|方面的结果"},
            {"的问题", "的这一问题|的核心问题|的具体问题|方面的问题"},
            {"的效果", "的实际效果|的具体效果|的最终效果|方面的效果"},
            {"的性能", "的整体性能|的实际性能|的综合性能|方面的性能"},
            {"的影响", "的实际影响|的具体影响|的潜在影响|方面的影响"},
            {"的需求", "的实际需求|的具体需求|的潜在需求|方面的需求"},
            {"的特征", "的具体特征|的典型特征|的主要特征|方面的特征"},
            {"的趋势", "的变化趋势|的总体趋势|的发展趋势|方面的趋势"},
            {"的变化", "的具体变化|的实际变化|的动态变化|方面的变化"},
            {"的关系", "的内在关系|的具体关系|的潜在关系|方面的关系"},
            {"的预测", "的具体预测|的初步预测|的定量预测|方面的预测"},
            {"的能力", "的实际能力|的综合能力|的整体能力|方面的能力"},
            
            // 打断"动词+了"高概率搭配
            {"提出了", "提出了一种|提出并验证了|针对性地提出了"},
            {"采用了", "采用了一种|尝试采用了|最终采用了"},
            {"构建了", "构建了一个|初步构建了|尝试构建了"},
            {"建立了", "建立了一个|初步建立了|尝试建立了"},
            {"实现了", "基本实现了|初步实现了|在一定程度上实现了"},
            {"取得了", "初步取得了|在一定程度上取得了|基本取得了"},
            {"验证了", "初步验证了|通过实验验证了|在一定程度上验证了"},
            {"证明了", "初步证明了|在一定程度上证明了|基本证明了"},
            {"发现了", "初步发现了|观察到并发现了|注意到并发现了"},
            
            // 打断"形容词+名词"高概率搭配
            {"重要的", "比较重要的|相对重要的|不可忽视的"},
            {"有效的", "相对有效的|比较有效的|可行且有效的"},
            {"主要的", "比较主要的|相对主要的|核心的"},
            {"关键的", "比较关键的|相对关键的|不容忽视的"},
            {"合理的", "比较合理的|相对合理的|基本合理的"},
            {"准确的", "比较准确的|相对准确的|基本准确的"},
            {"良好的", "比较良好的|相对不错的|还算良好的"},
            {"明显的", "比较明显的|相对明显的|肉眼可见的"},
    };
    
    // ========== 括号补充模板（增加人类写作的"打断-补充"特征） ==========
    private static final String[][] PARENTHETICAL_INSERTS = {
            {"数据", "数据（包括训练集和测试集）|数据（来源于实际采集）|数据（经过预处理后）"},
            {"模型", "模型（本文采用的）|模型（下文将详细介绍）|模型（基于深度学习的）"},
            {"算法", "算法（具体实现见后文）|算法（经过调参后的）|算法（本文选用的）"},
            {"方法", "方法（下文简称该方法）|方法（结合实际情况的）|方法（经过改进的）"},
            {"指标", "指标（如精度、召回率等）|指标（具体定义见下文）|指标（常用的评价标准）"},
            {"特征", "特征（包含时间和空间维度）|特征（经过提取后的）|特征（多维度的）"},
            {"网络", "网络（即神经网络结构）|网络（多层结构的）|网络（本文设计的）"},
            {"框架", "框架（整体结构见图）|框架（本文提出的）|框架（包含多个模块的）"},
            {"参数", "参数（经过实验调优的）|参数（具体取值见表）|参数（需要手动设定的）"},
            {"预测", "预测（短期的）|预测（基于历史数据的）|预测（带有一定误差的）"},
    };
    
    /**
     * 第一轮改写：基础变换（预处理，为AI改写做准备）
     */
    public String rewriteBasic(String text) {
        if (text == null || text.isEmpty()) return text;
        
        String result = text;
        
        // 0. 清理多余标记（字数提示、标题标记等）
        result = cleanExtraMarkers(result);
        
        // 1. 删除AI冗余修饰
        for (String phrase : REMOVE_PHRASES) {
            result = result.replace(phrase, "");
        }
        
        // 2. 同义词替换（2轮）
        for (int round = 0; round < 2; round++) {
            for (String[] pair : SYNONYM_DICT) {
                if (result.contains(pair[0])) {
                    String[] candidates = pair[1].split("\\|");
                    result = result.replace(pair[0], candidates[RANDOM.nextInt(candidates.length)]);
                }
            }
        }
        
        // 3. 打断高概率n-gram（核心！提高困惑度）
        result = breakNgrams(result);
        
        // 4. 句子拆分合并（制造突发性）
        result = adjustSentenceLength(result);
        
        // 5. 插入括号补充（人类写作特征）
        result = insertParenthetical(result);
        
        return result;
    }
    
    /**
     * 第二轮改写：深度变换（AI改写后的后处理）
     */
    public String rewriteDeep(String text) {
        if (text == null || text.isEmpty()) return text;
        
        String result = text;
        
        // 0. 清理多余标记
        result = cleanExtraMarkers(result);
        
        // 1. 多轮同义词替换
        for (int round = 0; round < 3; round++) {
            for (String[] pair : SYNONYM_DICT) {
                if (result.contains(pair[0])) {
                    String[] candidates = pair[1].split("\\|");
                    result = result.replace(pair[0], candidates[RANDOM.nextInt(candidates.length)]);
                }
            }
        }
        
        // 2. 打断高概率n-gram
        result = breakNgrams(result);
        
        // 3. 因果倒装（40%概率）
        result = invertCausality(result);
        
        // 4. 主被动转换
        result = convertActivePassive(result);
        
        // 5. 句子重排（保持逻辑连贯）
        result = shuffleSentences(result);
        
        // 6. 制造句子长度极端波动（突发性）
        result = adjustSentenceLength(result);
        
        // 7. 插入语气词和括号补充
        result = insertHumanMarkers(result);
        result = insertParenthetical(result);
        
        // 8. 插入破折号解释（人类写作特征）
        result = insertDashExplanation(result);
        
        return result;
    }
    
    /**
     * 打断高概率n-gram（核心方法）
     * 在常见搭配中间插入修饰语，使每个词不再是"最可能的下一个词"
     */
    private String breakNgrams(String text) {
        String result = text;
        int breakCount = 0;
        int maxBreaks = Math.max(3, text.length() / 80); // 每80字至少打断一处
        
        for (String[] pair : NGRAM_BREAKERS) {
            if (breakCount >= maxBreaks) break;
            if (result.contains(pair[0])) {
                String[] candidates = pair[1].split("\\|");
                String replacement = candidates[RANDOM.nextInt(candidates.length)];
                // 只替换第一次出现（避免过度替换）
                result = result.replaceFirst(Pattern.quote(pair[0]), Matcher.quoteReplacement(replacement));
                breakCount++;
            }
        }
        
        return result;
    }
    
    /**
     * 制造句子长度极端波动（突发性burstiness）
     * AI文本句子长度均匀，人类文本长短交替极端
     */
    private String adjustSentenceLength(String text) {
        String[] sentences = text.split("(?<=[。！？])");
        if (sentences.length <= 1) return text;
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < sentences.length; i++) {
            String sent = sentences[i];
            if (sent == null || sent.trim().isEmpty()) continue;
            
            int sentLen = sent.length();
            
            // 超长句拆分（>45字）—— 拆成不均匀的两段
            if (sentLen > 45) {
                String split = splitLongSentenceUneven(sent);
                if (split != null) {
                    result.append(split);
                    continue;
                }
            }
            
            // 超短句合并（<15字），合并后加个补充说明让它变长
            if (sentLen < 15 && i + 1 < sentences.length) {
                String next = sentences[i + 1];
                if (next != null && !next.trim().isEmpty()) {
                    String merged = sent.replaceAll("[。]$", "") + "，" + next.trim();
                    result.append(merged);
                    i++;
                    continue;
                }
            }
            
            result.append(sent);
        }
        
        return result.toString();
    }
    
    /**
     * 不均匀拆分长句（制造长短差异）
     * 不在中间拆，而是在1/3或2/3处拆，制造一长一短
     */
    private String splitLongSentenceUneven(String sent) {
        // 在1/3处找逗号
        int target1 = sent.length() / 3;
        int target2 = sent.length() * 2 / 3;
        
        // 随机选择在前1/3还是后2/3处拆分
        int target = RANDOM.nextBoolean() ? target1 : target2;
        int bestComma = -1;
        int bestDist = Integer.MAX_VALUE;
        
        for (int j = Math.max(5, target - 15); j < Math.min(sent.length() - 5, target + 15); j++) {
            if (sent.charAt(j) == '，') {
                int dist = Math.abs(j - target);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestComma = j;
                }
            }
        }
        
        if (bestComma > 0) {
            return sent.substring(0, bestComma) + "。" + sent.substring(bestComma + 1);
        }
        
        return null;
    }
    
    /**
     * 因果倒装（应用到整段文本）
     */
    private String invertCausality(String text) {
        String[] sentences = text.split("(?<=[。！？])");
        if (sentences.length <= 1) return text;
        
        StringBuilder result = new StringBuilder();
        for (String sent : sentences) {
            if (sent == null || sent.trim().isEmpty()) continue;
            
            if (RANDOM.nextInt(100) < 40) {
                String inverted = invertSingleSentence(sent);
                if (inverted != null) {
                    result.append(inverted);
                    continue;
                }
            }
            result.append(sent);
        }
        
        return result.toString();
    }
    
    /**
     * 单句因果倒装
     */
    private String invertSingleSentence(String sent) {
        Matcher m1 = Pattern.compile("^(由于|因为|鉴于)(.+?)，(.+)$").matcher(sent.trim());
        if (m1.matches()) {
            String cause = m1.group(2).trim();
            String effect = m1.group(3).trim();
            return effect.replaceAll("[。]$", "") + "，这主要是因为" + cause + "。";
        }
        
        Matcher m2 = Pattern.compile("^(.+?)，(因此|所以|从而)(.+)$").matcher(sent.trim());
        if (m2.matches()) {
            String cause = m2.group(1).trim();
            String effect = m2.group(3).trim();
            return effect.replaceAll("[。]$", "") + "，背后的原因在于" + cause + "。";
        }
        
        return null;
    }
    
    /**
     * 主被动转换
     */
    private String convertActivePassive(String text) {
        String result = text;
        result = result.replaceAll("([\\u4e00-\\u9fff]{2,6})提高了([\\u4e00-\\u9fff]{2,6})", "$2得到了提高");
        result = result.replaceAll("([\\u4e00-\\u9fff]{2,6})降低了([\\u4e00-\\u9fff]{2,6})", "$2得到了降低");
        result = result.replaceAll("([\\u4e00-\\u9fff]{2,6})改善了([\\u4e00-\\u9fff]{2,6})", "$2得到了改善");
        result = result.replaceAll("([\\u4e00-\\u9fff]{2,6})优化了([\\u4e00-\\u9fff]{2,6})", "$2得到了优化");
        result = result.replaceAll("([\\u4e00-\\u9fff]{2,6})增强了([\\u4e00-\\u9fff]{2,6})", "$2得到了增强");
        result = result.replaceAll("被([\\u4e00-\\u9fff]{2,4})所", "$1使得");
        return result;
    }
    
    /**
     * 句子重排（保持逻辑连贯）
     */
    private String shuffleSentences(String text) {
        String[] sentences = text.split("(?<=[。！？])");
        if (sentences.length <= 2) return text;
        
        for (int i = 0; i < sentences.length - 1; i++) {
            String current = sentences[i];
            String next = sentences[i + 1];
            if (current == null || next == null) continue;
            
            String nextTrim = next.trim();
            // 有逻辑连接词的不重排
            if (nextTrim.startsWith("因此") || nextTrim.startsWith("所以") || 
                nextTrim.startsWith("但") || nextTrim.startsWith("不过") ||
                nextTrim.startsWith("然而") || nextTrim.startsWith("同时") ||
                nextTrim.startsWith("此外") || nextTrim.startsWith("另外") ||
                nextTrim.startsWith("首先") || nextTrim.startsWith("其次") ||
                nextTrim.startsWith("最后") || nextTrim.startsWith("综上") ||
                nextTrim.startsWith("第一") || nextTrim.startsWith("第二") ||
                nextTrim.startsWith("第三")) {
                continue;
            }
            
            String currentTrim = current.trim();
            if (currentTrim.contains("首先") || currentTrim.contains("其次") || 
                currentTrim.contains("第一") || currentTrim.contains("第二")) {
                continue;
            }
            
            // 35%概率交换
            if (RANDOM.nextInt(100) < 35) {
                sentences[i] = next;
                sentences[i + 1] = current;
                i++;
            }
        }
        
        return String.join("", sentences);
    }
    
    /**
     * 插入人类化语气词
     */
    private String insertHumanMarkers(String text) {
        String[] sentences = text.split("(?<=[。！？])");
        if (sentences.length <= 2) return text;
        
        StringBuilder result = new StringBuilder();
        String[] markers = {
            "实际上，", "说实话，", "从这个角度看，",
            "简单来讲，", "具体点说，", "这里要提一下，",
            "有意思的是，", "需要注意，", "从实际情况看，",
            "大体上看，", "基本上，", "坦率地讲，",
            "客观来看，", "平心而论，"
        };
        
        for (int i = 0; i < sentences.length; i++) {
            String sent = sentences[i];
            if (sent == null || sent.trim().isEmpty()) continue;
            
            // 每2-3个句子插入一个语气词（30%概率）
            if (i > 0 && i % 2 == 0 && RANDOM.nextInt(100) < 30) {
                String marker = markers[RANDOM.nextInt(markers.length)];
                String trimmed = sent.trim();
                if (!trimmed.startsWith("因此") && !trimmed.startsWith("所以") && 
                    !trimmed.startsWith("但") && !trimmed.startsWith("不过") &&
                    !trimmed.startsWith("然而") && !trimmed.startsWith("同时")) {
                    sent = marker + trimmed;
                }
            }
            
            result.append(sent);
        }
        
        return result.toString();
    }
    
    /**
     * 插入括号补充说明（人类写作的典型特征）
     * AI很少使用括号做补充说明，人类经常这样做
     */
    private String insertParenthetical(String text) {
        String result = text;
        int insertCount = 0;
        int maxInserts = Math.max(1, text.length() / 200); // 每200字最多插一个
        
        for (String[] pair : PARENTHETICAL_INSERTS) {
            if (insertCount >= maxInserts) break;
            if (result.contains(pair[0]) && !result.contains(pair[0] + "（")) {
                String[] candidates = pair[1].split("\\|");
                String replacement = candidates[RANDOM.nextInt(candidates.length)];
                // 只替换第一次出现
                result = result.replaceFirst(Pattern.quote(pair[0]), Matcher.quoteReplacement(replacement));
                insertCount++;
            }
        }
        
        return result;
    }
    
    /**
     * 插入破折号解释（人类写作特征）
     * 在某些名词后面加"——即xxx"的解释
     */
    private String insertDashExplanation(String text) {
        String result = text;
        
        String[][] dashInserts = {
                {"深度学习", "深度学习——一种基于多层神经网络的机器学习方法"},
                {"卷积神经网络", "卷积神经网络——也就是CNN"},
                {"循环神经网络", "循环神经网络——即RNN"},
                {"注意力机制", "注意力机制——用于捕捉序列中的长距离依赖"},
                {"迁移学习", "迁移学习——将一个领域的知识迁移到另一个领域"},
                {"过拟合", "过拟合——即模型在训练集上表现好但泛化能力差"},
                {"特征提取", "特征提取——从原始数据中筛选有用信息"},
                {"聚类分析", "聚类分析——将相似的样本归为一类"},
        };
        
        // 最多插入1个破折号解释
        for (String[] pair : dashInserts) {
            if (result.contains(pair[0]) && !result.contains("——")) {
                // 30%概率
                if (RANDOM.nextInt(100) < 30) {
                    result = result.replaceFirst(Pattern.quote(pair[0]), Matcher.quoteReplacement(pair[1]));
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 清理多余标记：字数提示（约XXX字）、（标题）等
     * 这些标记来自论文生成时AI输出的元信息，不应出现在正文中
     */
    private String cleanExtraMarkers(String text) {
        String result = text;
        // （约240字）（约360字）等
        result = result.replaceAll("（约\\s*\\d+\\s*字）", "");
        result = result.replaceAll("\\(约\\s*\\d+\\s*字\\)", "");
        // （240字左右）等
        result = result.replaceAll("（\\d+\\s*字左右）", "");
        result = result.replaceAll("\\(\\d+\\s*字左右\\)", "");
        // （标题）
        result = result.replaceAll("（标题）", "");
        result = result.replaceAll("\\(标题\\)", "");
        // （约240个）
        result = result.replaceAll("（约\\s*\\d+\\s*个）", "");
        // 清理残留的空行
        result = result.replaceAll("(?m)^\\s*\\n", "\n");
        result = result.replaceAll("\\n{3,}", "\n\n");
        return result.trim();
    }
}
