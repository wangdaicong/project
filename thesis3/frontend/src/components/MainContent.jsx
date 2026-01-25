import React, { useState, useRef } from 'react';
import { createPortal } from 'react-dom';
import { 
  Sparkles, Upload, FileText, Download, RefreshCw, 
  Image, Table, Code, Calculator, Plus, Trash2, Edit3,
  ChevronDown, Check, Loader2
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { paperApi, fileApi } from '../api';
import toast from 'react-hot-toast';

const subjects = [
  '(智能识别)', '01哲学', '02经济学', '03法学', '04教育学', '05文学',
  '06历史学', '07理学', '08工学', '09农学', '10医学', '12管理学', '13艺术学'
];

const wordCounts = [
  { label: '3000字', value: 3000 },
  { label: '5000字', value: 5000 },
  { label: '8000字', value: 8000 },
  { label: '10000字', value: 10000 },
  { label: '15000字', value: 15000 },
  { label: '20000字', value: 20000 },
  { label: '30000字', value: 30000 },
];

const languages = [
  { label: '中文', value: '中文' },
  { label: '英文', value: '英文' },
];

function MainContent({ user, paperType, onLoginRequired, initialTitle, onTitleUsed }) {
  const [title, setTitle] = useState('');
  
  // 当从选题页面传入题目时，自动填充
  React.useEffect(() => {
    if (initialTitle) {
      setTitle(initialTitle);
      if (onTitleUsed) onTitleUsed();
    }
  }, [initialTitle, onTitleUsed]);
  const [subject, setSubject] = useState('(智能识别)');
  const [language, setLanguage] = useState('中文');
  const [wordCount, setWordCount] = useState(10000);
  const [outline, setOutline] = useState('');
  const [content, setContent] = useState('');
  const [referenceContent, setReferenceContent] = useState('');
  const [showUpload, setShowUpload] = useState(false);
  const [includeCharts, setIncludeCharts] = useState(false);
  const [includeImages, setIncludeImages] = useState(false);
  const [includeFormulas, setIncludeFormulas] = useState(false);
  const [includeCode, setIncludeCode] = useState(false);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [progress, setProgress] = useState(0);
  const [showDownloadMenu, setShowDownloadMenu] = useState(false);
  const [generationIncomplete, setGenerationIncomplete] = useState(false);
  const [missingSections, setMissingSections] = useState([]);
  const fileInputRef = useRef(null);
  const abortControllerRef = useRef(null);
  const downloadButtonRef = useRef(null);
  const downloadMenuRef = useRef(null);
  const [downloadMenuPos, setDownloadMenuPos] = useState({ top: 0, left: 0 });

  React.useEffect(() => {
    if (!showDownloadMenu) return;

    const updatePos = () => {
      const btn = downloadButtonRef.current;
      if (!btn) return;
      const rect = btn.getBoundingClientRect();
      const menuWidth = 176; // tailwind w-44 => 11rem
      const padding = 8;
      const left = Math.max(padding, rect.right - menuWidth);
      const top = rect.bottom + padding;
      setDownloadMenuPos({ top, left });
    };

    updatePos();

    const onDocMouseDown = (e) => {
      const btn = downloadButtonRef.current;
      const menu = downloadMenuRef.current;
      if (btn && btn.contains(e.target)) return;
      if (menu && menu.contains(e.target)) return;
      setShowDownloadMenu(false);
    };

    window.addEventListener('resize', updatePos);
    window.addEventListener('scroll', updatePos, true);
    document.addEventListener('mousedown', onDocMouseDown);
    return () => {
      window.removeEventListener('resize', updatePos);
      window.removeEventListener('scroll', updatePos, true);
      document.removeEventListener('mousedown', onDocMouseDown);
    };
  }, [showDownloadMenu]);

  const getCompletionMarkers = (outlineText) => {
    if (!outlineText) return [];
    const lines = outlineText.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
    const headings = lines
      .filter(l => /^#+\s+/.test(l))
      .map(l => l.replace(/^#+\s+/, '').replace(/\*\*/g, '').trim())
      .filter(Boolean);

    const important = headings.filter(h => /(第[一二三四五六七八九十\d]+章|参考文献|致谢)/.test(h));
    const list = (important.length > 0 ? important : headings).slice(-6);
    return Array.from(new Set(list));
  };

  const handleGenerateOutline = async () => {
    if (!title.trim()) {
      toast.error('请输入论文题目');
      return;
    }

    setLoading(true);
    try {
      const response = await paperApi.generateOutline({
        title,
        paperType,
        subject,
        languages: [language],
        wordCount,
        referenceContent: referenceContent || null
      });

      if (response.data.code === 200) {
        setOutline(response.data.data);
        toast.success('大纲生成成功！');
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      toast.error(error.response?.data?.message || error.message || '生成失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleGeneratePaper = async (mode = 'new') => {
    if (!outline.trim()) {
      toast.error('请先生成或输入大纲');
      return;
    }

    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    setGenerating(true);
    setProgress(0);
    setGenerationIncomplete(false);
    setMissingSections([]);
    const baseContent = mode === 'continue' ? content : '';
    let fullContent = baseContent;
    if (mode !== 'continue') {
      setContent('');
    }

    const controller = new AbortController();
    abortControllerRef.current = controller;

    try {
      const payload = {
        title,
        paperType,
        subject,
        languages: [language],
        wordCount,
        outline,
        previousContent: mode === 'continue' ? (baseContent.length > 4000 ? baseContent.slice(-4000) : baseContent) : null,
        referenceContent: referenceContent || null,
        includeCharts,
        includeImages,
        includeFormulas,
        includeCode
      };

      const resp = await fetch('/api/paper/generate/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload),
        signal: controller.signal
      });

      if (!resp.ok) {
        const errText = await resp.text().catch(() => '');
        throw new Error(errText || `请求失败: HTTP ${resp.status}`);
      }

      if (!resp.body) {
        throw new Error('浏览器不支持流式响应');
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let buffer = '';
      let receivedChars = 0;

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk;

        // SSE frames are delimited by blank line. But some servers may stream raw text.
        const frames = buffer.split(/\n\n/);
        buffer = frames.pop() || '';

        for (const frame of frames) {
          const lines = frame.split(/\r?\n/);
          const dataLines = lines
            .filter(l => l.startsWith('data:'))
            .map(l => l.replace(/^data:\s?/, ''));

          const text = dataLines.length > 0 ? dataLines.join('\n') : frame;
          if (!text) continue;

          receivedChars += text.length;
          fullContent += text;
          setContent(prev => prev + text);
          setProgress(prev => (prev < 95 ? Math.min(95, prev + 1) : prev));
        }
      }

      setProgress(100);
      const markers = getCompletionMarkers(outline);
      const missing = markers.filter(m => m && !fullContent.includes(m));
      if (missing.length > 0) {
        setGenerationIncomplete(true);
        setMissingSections(missing);
        toast('生成结束：检测到内容可能未完整，可点击继续生成', {
          style: { whiteSpace: 'nowrap' }
        });
      } else {
        toast.success('论文生成完成！');
      }
    } catch (error) {
      if (error.name === 'AbortError') {
        toast('已停止生成');
      } else {
        toast.error(error.message || '生成失败，请重试');
      }
    } finally {
      setGenerating(false);
      abortControllerRef.current = null;
    }
  };

  const handleStopGenerate = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
  };

  const handleFileUpload = async (e) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    try {
      const response = await fileApi.uploadMultiple(Array.from(files));
      if (response.data.code === 200) {
        setReferenceContent(prev => prev + '\n' + response.data.data);
        toast.success('文件上传成功！');
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      toast.error('文件上传失败');
    }
  };

  const downloadBlob = (blob, filename) => {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const sanitizeFilename = (name) => {
    const base = (name || '论文').trim() || '论文';
    return base
      .replace(/[\\/:*?"<>|]/g, '_')
      .replace(/\s+/g, ' ')
      .slice(0, 120);
  };

  const handleDownload = async (format) => {
    if (!content) {
      toast.error('请先生成论文');
      return;
    }

    const safeTitle = sanitizeFilename(title || '论文');

    if (format === 'md') {
      const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' });
      downloadBlob(blob, `${safeTitle}.md`);
      toast.success('下载成功！');
      return;
    }

    try {
      const resp = await paperApi.exportPaper(format, { title: safeTitle, content });
      const ext = format === 'pdf' ? 'pdf' : 'docx';
      const mime = format === 'pdf'
        ? 'application/pdf'
        : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
      const blob = new Blob([resp.data], { type: mime });
      downloadBlob(blob, `${safeTitle}.${ext}`);
      toast.success('下载成功！');
    } catch (e) {
      try {
        const data = e?.response?.data;
        if (data instanceof Blob) {
          const text = await data.text();
          try {
            const json = JSON.parse(text);
            toast.error(json.message || json.error || '下载失败，请重试');
          } catch {
            toast.error(text || '下载失败，请重试');
          }
          return;
        }
      } catch {
      }
      toast.error(e?.response?.status ? `下载失败（${e.response.status}）` : '下载失败，请重试');
    }
  };

  return (
    <main className="flex-1 p-6 overflow-y-auto">
      <div className="max-w-6xl mx-auto">
        {/* Header Banner */}
        <div className="glass-card p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-gray-800 flex items-center">
                <Sparkles className="w-7 h-7 mr-2 text-blue-500" />
                Easy AI 写作
              </h2>
              <p className="text-gray-500 mt-1">专业AI学术写作·无限改稿</p>
            </div>
            <div className="flex items-center space-x-2 text-sm text-gray-500">
              <span className="px-3 py-1 bg-blue-100 text-blue-600 rounded-full">最新AI5.0版</span>
              <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full">支持查重</span>
            </div>
          </div>
        </div>

        {/* Main Editor */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left Panel - Input */}
          <div className="glass-card p-6">
            <div className="space-y-5">
              {/* Paper Type */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">论文类型</label>
                <div className="px-4 py-3 bg-blue-50 text-blue-600 rounded-lg font-medium">
                  {paperType}
                </div>
              </div>

              {/* Title Input */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">论文题目</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="请输入论文题目，如：艾宾浩斯记忆曲线在初中英语教学中的运用研究"
                  className="input-field"
                />
              </div>

              {/* Subject Selection */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">学科领域</label>
                  <select
                    value={subject}
                    onChange={(e) => setSubject(e.target.value)}
                    className="select-field"
                  >
                    {subjects.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">语言</label>
                  <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="select-field"
                  >
                    {languages.map(l => (
                      <option key={l.value} value={l.value}>{l.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Word Count */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">选择字数</label>
                <div className="flex flex-wrap gap-2">
                  {wordCounts.map(wc => (
                    <button
                      key={wc.value}
                      onClick={() => setWordCount(wc.value)}
                      className={`paper-type-btn ${wordCount === wc.value ? 'active' : ''}`}
                    >
                      {wc.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Additional Options */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">高级选项</label>
                <div className="flex flex-wrap gap-3">
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={includeImages}
                      onChange={(e) => setIncludeImages(e.target.checked)}
                      className="w-4 h-4 text-blue-500 rounded"
                    />
                    <Image className="w-4 h-4 text-gray-500" />
                    <span className="text-sm text-gray-600">插入图</span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={includeCharts}
                      onChange={(e) => setIncludeCharts(e.target.checked)}
                      className="w-4 h-4 text-blue-500 rounded"
                    />
                    <Table className="w-4 h-4 text-gray-500" />
                    <span className="text-sm text-gray-600">数据表格</span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={includeFormulas}
                      onChange={(e) => setIncludeFormulas(e.target.checked)}
                      className="w-4 h-4 text-blue-500 rounded"
                    />
                    <Calculator className="w-4 h-4 text-gray-500" />
                    <span className="text-sm text-gray-600">数学公式</span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={includeCode}
                      onChange={(e) => setIncludeCode(e.target.checked)}
                      className="w-4 h-4 text-blue-500 rounded"
                    />
                    <Code className="w-4 h-4 text-gray-500" />
                    <span className="text-sm text-gray-600">代码示例</span>
                  </label>
                </div>
              </div>

              {/* Upload Section */}
              <div>
                <button
                  onClick={() => setShowUpload(!showUpload)}
                  className="flex items-center space-x-2 text-blue-500 hover:text-blue-600"
                >
                  <Upload className="w-4 h-4" />
                  <span className="text-sm">"投喂"AI - 上传参考资料</span>
                  <ChevronDown className={`w-4 h-4 transition-transform ${showUpload ? 'rotate-180' : ''}`} />
                </button>

                {showUpload && (
                  <div className="mt-3 p-4 bg-gray-50 rounded-lg">
                    <p className="text-xs text-gray-500 mb-3">
                      您可以提交资料让AI深度学习后再生成，支持txt、word、pdf、markdown文档
                    </p>
                    <div className="flex space-x-3">
                      <textarea
                        value={referenceContent}
                        onChange={(e) => setReferenceContent(e.target.value)}
                        placeholder="粘贴文本内容..."
                        className="flex-1 input-field h-24 resize-none"
                      />
                      <div>
                        <input
                          type="file"
                          ref={fileInputRef}
                          onChange={handleFileUpload}
                          multiple
                          accept=".txt,.doc,.docx,.pdf,.md"
                          className="hidden"
                        />
                        <button
                          onClick={() => fileInputRef.current?.click()}
                          className="btn-secondary h-full flex flex-col items-center justify-center px-6"
                        >
                          <Upload className="w-6 h-6 mb-1" />
                          <span className="text-xs">上传文档</span>
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Generate Outline Button */}
              <button
                onClick={handleGenerateOutline}
                disabled={loading}
                className="btn-primary w-full flex items-center justify-center space-x-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>生成中...</span>
                  </>
                ) : (
                  <>
                    <Sparkles className="w-5 h-5" />
                    <span>免费生成大纲</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Right Panel - Outline */}
          <div className="glass-card p-6 flex flex-col">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">论文大纲</h3>
              <div className="flex space-x-2">
                <button className="p-2 hover:bg-gray-100 rounded-lg" title="编辑">
                  <Edit3 className="w-4 h-4 text-gray-500" />
                </button>
                <button 
                  onClick={() => setOutline('')}
                  className="p-2 hover:bg-gray-100 rounded-lg" 
                  title="清空"
                >
                  <Trash2 className="w-4 h-4 text-gray-500" />
                </button>
              </div>
            </div>

            <textarea
              value={outline}
              onChange={(e) => setOutline(e.target.value)}
              placeholder="大纲将在这里显示，您也可以手动输入或修改大纲..."
              className="w-full flex-1 min-h-[400px] p-4 border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />

            {/* Generate Paper Button */}
            <button
              onClick={handleGeneratePaper}
              disabled={generating || !outline}
              className="btn-primary w-full mt-4 flex items-center justify-center space-x-2"
            >
              {generating ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  <span>生成中... {Math.round(progress)}%</span>
                </>
              ) : (
                <>
                  <FileText className="w-5 h-5" />
                  <span>生成论文正文</span>
                </>
              )}
            </button>

            {generating && (
              <button
                onClick={handleStopGenerate}
                className="btn-secondary w-full mt-2"
              >
                停止生成
              </button>
            )}

            {generating && (
              <div className="mt-3">
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-gradient-to-r from-blue-500 to-purple-600 transition-all duration-500"
                    style={{ width: `${progress}%` }}
                  />
                </div>
                <p className="text-xs text-gray-500 mt-2 text-center">
                  检索文献中，请耐心等待...
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Paper Content */}
        {content && (
          <div className="glass-card p-6 mt-6">
            <div className="flex items-start justify-between mb-4 gap-3">
              <h3 className="text-lg font-semibold text-gray-800 flex-1 min-w-0 break-words">
                论文正文 | {title} | {paperType} | {wordCount/10000}万字
              </h3>
              <div className="flex flex-nowrap items-center gap-2 shrink-0 whitespace-nowrap overflow-visible">
                <button
                  onClick={() => handleGeneratePaper('new')}
                  className="btn-secondary flex items-center space-x-2"
                >
                  <RefreshCw className="w-4 h-4" />
                  <span className="whitespace-nowrap">重新生成</span>
                </button>
                {generationIncomplete && !generating && (
                  <button
                    onClick={() => handleGeneratePaper('continue')}
                    className="btn-secondary flex items-center space-x-2"
                  >
                    <Plus className="w-4 h-4" />
                    <span className="whitespace-nowrap">继续生成</span>
                  </button>
                )}
                <div className="relative">
                  <button
                    ref={downloadButtonRef}
                    onClick={() => setShowDownloadMenu(v => !v)}
                    className="btn-primary flex items-center space-x-2"
                  >
                    <Download className="w-4 h-4" />
                    <span className="whitespace-nowrap">下载论文</span>
                    <ChevronDown className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

            {showDownloadMenu && createPortal(
              <div
                ref={downloadMenuRef}
                className="w-44 bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden"
                style={{ position: 'fixed', top: downloadMenuPos.top, left: downloadMenuPos.left }}
              >
                <button
                  onClick={() => { setShowDownloadMenu(false); handleDownload('docx'); }}
                  className="w-full text-left px-4 py-2 text-sm hover:bg-gray-50"
                >
                  下载 Word (.docx)
                </button>
                <button
                  onClick={() => { setShowDownloadMenu(false); handleDownload('pdf'); }}
                  className="w-full text-left px-4 py-2 text-sm hover:bg-gray-50"
                >
                  下载 PDF (.pdf)
                </button>
                <button
                  onClick={() => { setShowDownloadMenu(false); handleDownload('md'); }}
                  className="w-full text-left px-4 py-2 text-sm hover:bg-gray-50"
                >
                  下载 Markdown (.md)
                </button>
              </div>,
              document.body
            )}

            <div className="border border-gray-200 rounded-lg p-6 bg-white max-h-[600px] overflow-y-auto">
              <div className="markdown-content prose max-w-none">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>{content}</ReactMarkdown>
              </div>
            </div>
          </div>
        )}

        {/* Features List */}
        <div className="glass-card p-6 mt-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">平台特色</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              '最新AI5.0版，专业高质量',
              '支持知网最新版查AIGC',
              '免费大纲，支持三级大纲',
              '支持数据表、图、公式、代码',
              '40篇真实参考文献(带标注)',
              '支持"投喂AI"学习资料',
              '自动降AIGC率',
              '支持多语言写作'
            ].map((feature, i) => (
              <div key={i} className="flex items-center space-x-2 text-sm text-gray-600">
                <Check className="w-4 h-4 text-green-500 flex-shrink-0" />
                <span>{feature}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

export default MainContent;
