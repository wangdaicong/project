import React, { useState, useRef } from 'react';
import { Upload, FileText, Loader2, Download, Copy, Check, Sparkles, Shield, Zap, Clock, FileCheck } from 'lucide-react';
import toast from 'react-hot-toast';

const LANGUAGES = [
  { value: 'zh', label: '中文' },
  { value: 'en', label: '英语' },
  { value: 'ja', label: '日语' },
  { value: 'ko', label: '韩语' },
  { value: 'ru', label: '俄语' }
];

const SUBMIT_MODES = [
  { value: 'upload', label: '上传文档' },
  { value: 'paste', label: '粘贴文本' }
];

function PaperPassPage() {
  const [language, setLanguage] = useState('zh');
  const [submitMode, setSubmitMode] = useState('paste');
  const [inputText, setInputText] = useState('');
  const [outputText, setOutputText] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [copied, setCopied] = useState(false);
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      validateAndSetFile(file);
    }
  };

  const validateAndSetFile = (file) => {
    const validTypes = [
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'text/plain'
    ];
    const validExtensions = ['.doc', '.docx', '.txt'];
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!validTypes.includes(file.type) && !validExtensions.includes(ext)) {
      toast.error('仅支持 doc、docx、txt 格式文件');
      return;
    }
    
    if (file.size > 100 * 1024 * 1024) {
      toast.error('文件大小不能超过 100MB');
      return;
    }
    
    setUploadedFile(file);
    toast.success(`已选择文件: ${file.name}`);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.remove('border-blue-500', 'bg-blue-50');
    
    const file = e.dataTransfer.files?.[0];
    if (file) {
      validateAndSetFile(file);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.add('border-blue-500', 'bg-blue-50');
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.remove('border-blue-500', 'bg-blue-50');
  };

  const handleProcess = async () => {
    let textToProcess = '';
    
    if (submitMode === 'upload') {
      if (!uploadedFile) {
        toast.error('请先上传文件');
        return;
      }
      try {
        const formData = new FormData();
        formData.append('file', uploadedFile);
        const uploadResp = await fetch('/api/file/upload', {
          method: 'POST',
          body: formData
        });
        const uploadResult = await uploadResp.json();
        if (uploadResult.code !== 200 || !uploadResult.data) {
          toast.error(uploadResult.message || '文件解析失败');
          return;
        }
        textToProcess = uploadResult.data;
      } catch {
        toast.error('读取文件失败');
        return;
      }
    } else {
      textToProcess = inputText.trim();
      if (!textToProcess) {
        toast.error('请输入需要处理的文本');
        return;
      }
    }

    if (textToProcess.length < 10) {
      toast.error('文本内容过短，请输入更多内容');
      return;
    }

    setProcessing(true);
    setProgress(0);
    setOutputText('');

    try {
      const response = await fetch('/api/paperpass/reduce', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          content: textToProcess,
          language: language
        })
      });

      if (!response.ok) {
        throw new Error('处理失败，请重试');
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let result = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        if (chunk) {
          result += chunk;
          const cleanResult = result.replace(/data:/g, '').trim();
          setOutputText(cleanResult);
          setProgress(prev => Math.min(95, prev + 1));
        }
      }

      setProgress(100);
      toast.success('降重完成！');
    } catch (error) {
      toast.error(error.message || '处理失败，请重试');
    } finally {
      setProcessing(false);
    }
  };

  const handleCopy = async () => {
    if (!outputText) return;
    try {
      await navigator.clipboard.writeText(outputText);
      setCopied(true);
      toast.success('已复制到剪贴板');
      setTimeout(() => setCopied(false), 2000);
    } catch {
      toast.error('复制失败');
    }
  };

  const handleDownload = () => {
    if (!outputText) return;
    const blob = new Blob([outputText], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '降重结果.txt';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <main className="flex-1 p-6 bg-gray-50 min-h-screen">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-green-600 to-blue-600 bg-clip-text text-transparent mb-2">
            强力降论文查重率 无忧过审
          </h1>
          <p className="text-gray-500">疑似度降低 60%</p>
          <p className="text-lg text-gray-600 mt-2">一键降低 论文aigc率 ai痕迹 查重率</p>
        </div>

        <div className="flex justify-center gap-4 mb-6">
          <button className="bg-gradient-to-r from-green-500 to-blue-500 text-white px-6 py-3 rounded-xl font-medium hover:shadow-lg transition-all flex items-center gap-2">
            <FileCheck className="w-5 h-5" />
            立即降查重率
          </button>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="mb-4 text-sm text-gray-500">
            *以上仅为展示示例，具体结果以实际为准
          </div>
          
          <div className="text-sm text-gray-600 mb-6 p-4 bg-green-50 rounded-lg">
            说明：在下面粘贴论文内容或者上传论文原稿文件，上传论文原稿效果最佳
          </div>

          <div className="grid grid-cols-2 gap-6 mb-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">语言</label>
              <div className="flex flex-wrap gap-2">
                {LANGUAGES.map(lang => (
                  <button
                    key={lang.value}
                    onClick={() => setLanguage(lang.value)}
                    className={`px-4 py-2 rounded-lg text-sm transition-all ${
                      language === lang.value
                        ? 'bg-green-500 text-white'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                  >
                    {lang.label}
                  </button>
                ))}
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">提交方式</label>
              <div className="flex gap-2">
                {SUBMIT_MODES.map(mode => (
                  <button
                    key={mode.value}
                    onClick={() => setSubmitMode(mode.value)}
                    className={`px-4 py-2 rounded-lg text-sm transition-all ${
                      submitMode === mode.value
                        ? 'bg-green-500 text-white'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                  >
                    {mode.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {submitMode === 'upload' ? (
            <div
              ref={dropZoneRef}
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onClick={() => fileInputRef.current?.click()}
              className="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center cursor-pointer hover:border-green-400 transition-colors"
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".doc,.docx,.txt"
                onChange={handleFileSelect}
                className="hidden"
              />
              <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              {uploadedFile ? (
                <div className="flex items-center justify-center gap-2 text-green-600">
                  <FileText className="w-5 h-5" />
                  <span>{uploadedFile.name}</span>
                </div>
              ) : (
                <>
                  <p className="text-gray-600 mb-2">点击或拖放文件到这里上传</p>
                  <p className="text-sm text-gray-400">支持doc、docx、txt格式文件，文件大小不超过100M</p>
                </>
              )}
            </div>
          ) : (
            <textarea
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="请在此粘贴需要降低查重率的论文内容..."
              className="w-full h-64 p-4 border border-gray-200 rounded-xl resize-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
            />
          )}

          <div className="mt-6 flex justify-center">
            <button
              onClick={handleProcess}
              disabled={processing}
              className="bg-gradient-to-r from-green-500 to-blue-500 text-white px-8 py-3 rounded-xl font-medium hover:shadow-lg transition-all text-lg flex items-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  处理中... {progress}%
                </>
              ) : (
                <>
                  <FileCheck className="w-5 h-5" />
                  开始降查重率
                </>
              )}
            </button>
          </div>

          {processing && (
            <div className="mt-4">
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-gradient-to-r from-green-500 to-blue-500 transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          )}
        </div>

        {outputText && (
          <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">降重结果</h3>
              <div className="flex gap-2">
                <button
                  onClick={handleCopy}
                  className="btn-secondary flex items-center gap-2"
                >
                  {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                  {copied ? '已复制' : '复制'}
                </button>
                <button
                  onClick={handleDownload}
                  className="btn-secondary flex items-center gap-2"
                >
                  <Download className="w-4 h-4" />
                  下载
                </button>
              </div>
            </div>
            <div className="p-4 bg-gray-50 rounded-xl max-h-96 overflow-y-auto">
              <pre className="whitespace-pre-wrap text-gray-700 font-sans">{outputText}</pre>
            </div>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">使用说明</h3>
          <ul className="space-y-2 text-sm text-gray-600">
            <li className="flex items-start gap-2">
              <span className="text-green-500 font-bold">1.</span>
              本站降重和降aigc适用于主流查重平台；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-green-500 font-bold">2.</span>
              直接上传原文件效果最佳；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-green-500 font-bold">3.</span>
              支持中文、英语、日语、韩语、俄语等语言；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-green-500 font-bold">4.</span>
              安全保障：HTTPS加密传输，检测记录不留存，符合GDPR及学术伦理规范；
            </li>
          </ul>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
              <Shield className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h4 className="font-medium text-gray-800">安全护航</h4>
              <p className="text-xs text-gray-500">银行级加密保障</p>
            </div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
              <Check className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h4 className="font-medium text-gray-800">隐私保障</h4>
              <p className="text-xs text-gray-500">绝不收录论文</p>
            </div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
              <Zap className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <h4 className="font-medium text-gray-800">透明收费</h4>
              <p className="text-xs text-gray-500">按字符数计费</p>
            </div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
              <Clock className="w-5 h-5 text-orange-600" />
            </div>
            <div>
              <h4 className="font-medium text-gray-800">快速处理</h4>
              <p className="text-xs text-gray-500">实时流式输出</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">常见问题</h3>
          <div className="space-y-4">
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-green-600 mb-2">Q【安全护航】值得信任</h4>
              <p className="text-sm text-gray-600">已为200万用户守护创作成果，银行级加密技术保障数据安全，检测全程可随时销毁记录，放心查重零风险！</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-green-600 mb-2">Q【隐私保障】查重后会收录我的论文吗？</h4>
              <p className="text-sm text-gray-600">绝对不收录！系统自动粉碎记录，也支持下载后永久删除，泄密我们承担法律责任！</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-green-600 mb-2">Q【透明收费】请问是怎么计费的？</h4>
              <p className="text-sm text-gray-600">按字符数（不含空格/标点）计费！系统智能统计字符，无任何隐藏收费。</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-green-600 mb-2">Q【大文件处理】文档超过20MB怎么办？</h4>
              <p className="text-sm text-gray-600">删除图片后再上传文件，或者直接粘贴word文本内容进行降重</p>
            </div>
            <div>
              <h4 className="font-medium text-green-600 mb-2">Q【多种方式】无法上传论文？</h4>
              <p className="text-sm text-gray-600">推荐「粘贴文本模式」：Ctrl+A全选文字，30秒完成提交，检测结果完全一致！</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default PaperPassPage;
