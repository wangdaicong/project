import React, { useState } from 'react';
import { Sparkles, Search, Copy, ArrowRight, Loader2, Lightbulb } from 'lucide-react';
import toast from 'react-hot-toast';

const paperTypes = [
  { label: '毕业论文', value: '毕业论文' },
  { label: '期刊论文', value: '期刊论文' },
  { label: '职称论文', value: '职称论文' },
  { label: '课题/研究', value: '课题/研究' },
  { label: '课程/结课', value: '课程/结课' },
  { label: '其它', value: '其它' },
];

const countOptions = [5, 10, 15, 20, 30];

function TopicPage({ onSelectTopic }) {
  const [direction, setDirection] = useState('');
  const [paperType, setPaperType] = useState('毕业论文');
  const [count, setCount] = useState(10);
  const [loading, setLoading] = useState(false);
  const [topics, setTopics] = useState([]);

  const parseTopicsFromRaw = (raw) => {
    const makeTopic = (title, description = '', keywords = []) => ({
      title: (title || '').trim(),
      description: (description || '').trim(),
      keywords: Array.isArray(keywords) ? keywords : []
    });

    let s = (raw || '').trim();
    if (!s) return [];

    s = s.replace(/```\s*json\s*/gi, '').replace(/```/g, '').trim();

    const firstBrace = s.indexOf('{');
    const lastBrace = s.lastIndexOf('}');
    if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
      s = s.slice(firstBrace, lastBrace + 1).trim();
    }

    try {
      const parsed = JSON.parse(s);
      const list = Array.isArray(parsed?.topics) ? parsed.topics : [];
      const normalized = list.map((t) => {
        if (typeof t === 'string') {
          return makeTopic(t);
        }
        return makeTopic(t?.title, t?.description, t?.keywords);
      }).filter(t => t.title);
      if (normalized.length > 0) return normalized;
    } catch {
    }

    const titles = [];
    const titleRegex = /"title"\s*:\s*"([^\"]+)"/g;
    let m;
    while ((m = titleRegex.exec(s)) !== null) {
      const t = (m[1] || '').trim();
      if (t) titles.push(t);
    }
    if (titles.length > 0) {
      return titles.map(t => makeTopic(t));
    }

    const lines = (raw || '').split('\n')
      .map(l => l.trim())
      .filter(l => l);

    const filtered = lines
      .map(line => line.replace(/^```\s*json\s*/i, '').replace(/^```/i, '').replace(/```$/i, '').trim())
      .filter(line => line)
      .filter(line => !/^[\{\}\[\],]*$/.test(line))
      .filter(line => !/^"?(topics|title|description|keywords)"?\s*:/.test(line));

    return filtered
      .map((line) => line.replace(/^\d+[\.\、\)]?\s*/, '').replace(/^"|",?$|"$/g, '').trim())
      .filter(t => t)
      .map(t => makeTopic(t));
  };

  const handleGenerate = async () => {
    if (!direction.trim()) {
      toast.error('请输入论文方向或关键词');
      return;
    }

    setLoading(true);
    setTopics([]);

    try {
      const response = await fetch('/api/topic/suggest', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          direction,
          paperType,
          count
        })
      });

      const result = await response.json();

      if (result.code === 200 && result.data) {
        const parsedTopics = parseTopicsFromRaw(result.data);
        if (parsedTopics.length > 0) {
          setTopics(parsedTopics);
          toast.success(`成功生成 ${parsedTopics.length} 个选题建议！`);
        } else {
          toast.error('解析选题结果失败');
        }
      } else {
        toast.error(result.message || '生成失败');
      }
    } catch (error) {
      toast.error(error.message || '生成选题失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (title) => {
    navigator.clipboard.writeText(title);
    toast.success('已复制到剪贴板');
  };

  const handleUseTopic = (title) => {
    if (onSelectTopic) {
      onSelectTopic(title);
      toast.success('已选择该题目，请前往写作页面');
    }
  };

  return (
    <main className="flex-1 p-6 overflow-y-auto">
      <div className="max-w-6xl mx-auto">

        <div className="glass-card p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-gray-800 flex items-center">
                <Lightbulb className="w-7 h-7 mr-2 text-yellow-500" />
                论文智能选题
              </h2>
              <p className="text-gray-500 mt-1">论文题目智能生成，选题依据生成，一键搞定论文题目怎么定的烦恼！</p>
            </div>
            <div className="flex items-center space-x-2 text-sm text-gray-500">
              <span className="px-3 py-1 bg-blue-100 text-blue-600 rounded-full">智能选题</span>
              <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full">一键带入写作</span>
            </div>
          </div>
        </div>

        {/* Input Form */}
        <div className="glass-card p-6 mb-6">
          {/* Direction Input */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">论文方向</label>
            <textarea
              value={direction}
              onChange={(e) => setDirection(e.target.value)}
              placeholder="在这里输入论文关键词和写作要求等，越详细越好\n例如：人工智能在医疗诊断中的应用研究"
              className="w-full h-32 p-4 border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
          </div>

          {/* Paper Type */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">论文类型</label>
            <div className="flex flex-wrap gap-2">
              {paperTypes.map(pt => (
                <button
                  key={pt.value}
                  onClick={() => setPaperType(pt.value)}
                  className={`paper-type-btn ${paperType === pt.value ? 'active' : ''}`}
                >
                  {pt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Count */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">生成数量</label>
            <div className="flex flex-wrap gap-2">
              {countOptions.map(c => (
                <button
                  key={c}
                  onClick={() => setCount(c)}
                  className={`paper-type-btn ${count === c ? 'active' : ''}`}
                >
                  {c}个
                </button>
              ))}
            </div>
          </div>

          {/* Generate Button */}
          <button
            onClick={handleGenerate}
            disabled={loading}
            className="btn-primary w-full flex items-center justify-center space-x-2"
          >
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                <span>正在生成选题...</span>
              </>
            ) : (
              <>
                <Search className="w-5 h-5" />
                <span>生成选题</span>
              </>
            )}
          </button>
        </div>

        {/* Results */}
        {topics.length > 0 && (
          <div className="glass-card p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
              <Sparkles className="w-5 h-5 mr-2 text-blue-500" />
              选题建议 ({topics.length}个)
            </h3>

            <div className="space-y-4">
              {topics.map((topic, index) => (
                <div
                  key={index}
                  className="p-4 bg-white border border-gray-200 rounded-lg hover:shadow-md transition-shadow"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center mb-2">
                        <span className="w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium mr-3">
                          {index + 1}
                        </span>
                        <h3 className="text-gray-800 font-medium">{topic.title}</h3>
                      </div>
                      
                      {topic.description && (
                        <p className="text-sm text-gray-500 ml-9 mb-2">{topic.description}</p>
                      )}
                      
                      {topic.keywords && topic.keywords.length > 0 && (
                        <div className="ml-9 flex flex-wrap gap-2">
                          {topic.keywords.map((kw, ki) => (
                            <span
                              key={ki}
                              className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded"
                            >
                              {kw}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>

                    <div className="flex items-center space-x-2 ml-4">
                      <button
                        onClick={() => handleCopy(topic.title)}
                        className="p-2 text-gray-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                        title="复制题目"
                      >
                        <Copy className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleUseTopic(topic.title)}
                        className="px-3 py-1.5 bg-blue-500 text-white text-sm rounded-lg hover:bg-blue-600 transition-colors flex items-center space-x-1"
                      >
                        <span>使用</span>
                        <ArrowRight className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tips */}
        <div className="glass-card p-6 mt-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">选题技巧</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[
              '输入的关键词越详细，生成的题目越精准',
              '可以输入多个关键词，用空格或逗号分隔',
              '选择合适的论文类型和字数，有助于生成更匹配的题目',
              '点击"使用"按钮可直接将题目带入论文写作页面'
            ].map((tip, i) => (
              <div key={i} className="flex items-start space-x-2 text-sm text-gray-600">
                <span className="w-2 h-2 bg-blue-500 rounded-full mt-1.5 flex-shrink-0" />
                <span>{tip}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

export default TopicPage;
