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
    const { title, subject, paperType, wordCount, educationLevel, requirements, modelMode, outlineDepth, language } = await request.json();

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
    const depth = String(outlineDepth || "2") === "3" ? "3" : "2";
    const detailMode = String(modelMode || "standard") === "genius" ? "genius" : "standard";

    const systemPrompt = `你是一个专业的学术论文写作助手。请根据用户提供的论文信息，生成一份详细的论文大纲。

输出要求：
1. 输出语言：${outputLanguage}
2. 大纲层级：${depth === "3" ? "至少到三级标题" : "到二级标题即可"}
3. 详细程度：${detailMode === "genius" ? "更详细、更学术、更丰富" : "标准"}

大纲应该包含：
1. 摘要部分说明
2. 关键词建议
3. 引言/绪论
4. 文献综述
5. 研究方法/理论框架
6. 主体内容（根据论文类型分为多个章节）
7. 结论与展望
8. 参考文献说明

请使用清晰的层级结构，用数字和字母标注各级标题。`;

    const userPrompt = `请为以下论文生成详细大纲：

论文题目：${title}
${subject ? `学科专业：${subject}` : ""}
${paperType ? `论文类型：${paperType}` : ""}
${wordCount ? `字数要求：${wordCount}字` : ""}
${educationLevel ? `学历层次：${educationLevel}` : ""}
${requirements ? `其他要求：${requirements}` : ""}

请确保输出语言为：${outputLanguage}
请确保大纲层级为：${depth === "3" ? "三级大纲（三级标题要足够丰富）" : "二级大纲"}

请生成一份结构完整、逻辑清晰的论文大纲。`;

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
        max_tokens: 4096,
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
    console.error("Generate outline error:", error);
    const message = error instanceof Error ? error.message : "生成大纲时发生错误，请稍后重试";
    return new Response(message, { status: 500 });
  }
}
