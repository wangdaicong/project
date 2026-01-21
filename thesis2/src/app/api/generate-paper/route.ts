import { NextRequest } from "next/server";

const LLM_BASE_URL = process.env.LLM_BASE_URL || "http://127.0.0.1:1234/v1";
const LLM_MODEL = process.env.LLM_MODEL || "qwen2.5-3b-instruct";
const LLM_API_KEY = process.env.LLM_API_KEY || process.env.DEEPSEEK_API_KEY || process.env.OPENAI_API_KEY;

function getOpenAICompatibleBaseUrl(rawBaseUrl: string) {
  const trimmed = String(rawBaseUrl || "").trim().replace(/\/+$/, "");

  try {
    const u = new URL(trimmed);
    const pathname = (u.pathname || "/").replace(/\/+$/, "");
    if (pathname === "" || pathname === "/") {
      u.pathname = "/v1";
    } else if (!pathname.endsWith("/v1")) {
      u.pathname = `${pathname}/v1`;
    }
    return u.toString().replace(/\/+$/, "");
  } catch {
    return trimmed.endsWith("/v1") ? trimmed : `${trimmed}/v1`;
  }
}

export async function POST(request: NextRequest) {
  try {
    const { title, subject, paperType, wordCount, educationLevel, requirements, outline, modelMode, language } = await request.json();

    const OPENAI_BASE_URL = getOpenAICompatibleBaseUrl(LLM_BASE_URL);

    const languageMap: Record<string, string> = {
      zh: "中文",
      en: "英语",
      ja: "日语",
      ko: "韩语",
      ru: "俄语",
      th: "泰语",
    };
    const outputLanguage = languageMap[String(language || "zh")] || "中文";
    const detailMode = String(modelMode || "standard") === "genius" ? "genius" : "standard";

    const systemPrompt = `你是一个专业的学术论文写作助手。请根据用户提供的论文大纲，生成一篇完整的学术论文。

输出要求：
1. 输出语言：${outputLanguage}
2. 详细程度：${detailMode === "genius" ? "更详细、更学术、更丰富" : "标准"}

写作要求：
1. 严格按照大纲结构展开
2. 使用学术化的语言风格
3. 论述要有逻辑性和条理性
4. 适当引用相关理论和研究
5. 每个章节要有充实的内容
6. 注意段落之间的过渡和衔接
7. 结论要总结全文并提出展望

注意：生成的内容仅供学习参考，请用户进行人工审核和修改。`;

    const userPrompt = `请根据以下信息和大纲，撰写一篇完整的学术论文：

论文题目：${title}
${subject ? `学科专业：${subject}` : ""}
${paperType ? `论文类型：${paperType}` : ""}
${wordCount ? `字数要求：约${wordCount}字` : ""}
${educationLevel ? `学历层次：${educationLevel}` : ""}
${requirements ? `其他要求：${requirements}` : ""}

写作语言：${outputLanguage}

论文大纲：
${outline}

请按照大纲结构，撰写完整的论文内容。`;

    const response = await fetch(`${OPENAI_BASE_URL}/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(LLM_API_KEY ? { Authorization: `Bearer ${LLM_API_KEY}` } : {}),
      },
      body: JSON.stringify({
        model: LLM_MODEL,
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: userPrompt },
        ],
        stream: true,
        temperature: 0.7,
        max_tokens: 16384,
      }),
    });

    if (!response.ok) {
      const errText = await response.text().catch(() => "");
      throw new Error(`LLM API error: ${response.status}${errText ? ` - ${errText}` : ""}`);
    }

    const encoder = new TextEncoder();
    const stream = new ReadableStream({
      async start(controller) {
        const reader = response.body?.getReader();
        if (!reader) {
          controller.close();
          return;
        }

        const decoder = new TextDecoder();
        
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value);
          const lines = chunk.split("\n");

          for (const line of lines) {
            if (line.startsWith("data: ")) {
              const data = line.slice(6);
              if (data === "[DONE]") continue;
              
              try {
                const json = JSON.parse(data);
                const content = json.choices?.[0]?.delta?.content;
                if (content) {
                  controller.enqueue(encoder.encode(content));
                }
              } catch {
                // Skip invalid JSON
              }
            }
          }
        }

        controller.close();
      },
    });

    return new Response(stream, {
      headers: {
        "Content-Type": "text/plain; charset=utf-8",
        "Transfer-Encoding": "chunked",
      },
    });
  } catch (error) {
    console.error("Generate paper error:", error);
    const message = error instanceof Error ? error.message : "生成论文时发生错误，请稍后重试";
    return new Response(message, { status: 500 });
  }
}
