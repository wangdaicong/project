import React, { useState, useRef } from 'react';
import { Upload, FileText, Loader2, Download, Presentation, Sparkles, Shield, Zap, Clock, Check } from 'lucide-react';
import toast from 'react-hot-toast';

function PptPage() {
  const [inputText, setInputText] = useState('');
  const [title, setTitle] = useState('');
  const [pptOutline, setPptOutline] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [submitMode, setSubmitMode] = useState('upload');
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);

  const SUBMIT_MODES = [
    { value: 'upload', label: '上传文档' },
    { value: 'paste', label: '粘贴文本' }
  ];

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      validateAndSetFile(file);
    }
  };

  const validateAndSetFile = (file) => {
    const validExtensions = ['.doc', '.docx', '.txt'];
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!validExtensions.includes(ext)) {
      toast.error('仅支持 doc、docx、txt 格式文件');
      return;
    }
    
    if (file.size > 100 * 1024 * 1024) {
      toast.error('文件大小不能超过 100MB');
      return;
    }
    
    setUploadedFile(file);
    const nameWithoutExt = file.name.replace(/\.[^/.]+$/, '');
    setTitle(nameWithoutExt);
    toast.success(`已选择文件: ${file.name}`);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.remove('border-orange-500', 'bg-orange-50');
    
    const file = e.dataTransfer.files?.[0];
    if (file) {
      validateAndSetFile(file);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.add('border-orange-500', 'bg-orange-50');
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZoneRef.current?.classList.remove('border-orange-500', 'bg-orange-50');
  };

  const handleGenerate = async () => {
    let textToProcess = '';
    
    if (submitMode === 'upload') {
      if (!uploadedFile) {
        toast.error('请先上传论文文件');
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
        toast.error('请输入论文内容');
        return;
      }
    }

    if (textToProcess.length < 100) {
      toast.error('论文内容过短，请输入更多内容');
      return;
    }

    if (!title.trim()) {
      toast.error('请输入论文标题');
      return;
    }

    setProcessing(true);
    setProgress(0);
    setPptOutline('');

    try {
      const response = await fetch('/api/ppt/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: title.trim(),
          content: textToProcess
        })
      });

      if (!response.ok) {
        throw new Error('生成失败，请重试');
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
          setPptOutline(cleanResult);
          setProgress(prev => Math.min(95, prev + 1));
        }
      }

      setProgress(100);
      toast.success('PPT大纲生成完成！');
    } catch (error) {
      toast.error(error.message || '生成失败，请重试');
    } finally {
      setProcessing(false);
    }
  };

  const handleDownloadPptx = async () => {
    if (!pptOutline) {
      toast.error('请先生成PPT大纲');
      return;
    }

    try {
      const response = await fetch('/api/ppt/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: title.trim(),
          outline: pptOutline
        }),
        responseType: 'blob'
      });

      if (!response.ok) {
        throw new Error('导出失败');
      }

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${title || '答辩PPT'}.pptx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      toast.success('PPT下载成功！');
    } catch (error) {
      toast.error(error.message || '导出失败');
    }
  };

  return (
    <main className="flex-1 p-6 overflow-y-auto">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-full bg-white rounded-2xl shadow-sm px-8 py-6">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-orange-500 to-red-500 bg-clip-text text-transparent mb-2">
              一键生成答辩PPT
            </h1>
            <p className="text-lg text-gray-800">专业精美PPT 毕业答辩对着念 高效轻松拿捏！</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">论文标题</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="请输入论文标题"
              className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">提交方式</label>
            <div className="flex gap-2">
              {SUBMIT_MODES.map(mode => (
                <button
                  key={mode.value}
                  onClick={() => setSubmitMode(mode.value)}
                  className={`px-4 py-2 rounded-lg text-sm transition-all ${
                    submitMode === mode.value
                      ? 'bg-orange-500 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {mode.label}
                </button>
              ))}
            </div>
          </div>

          {submitMode === 'upload' ? (
            <div
              ref={dropZoneRef}
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onClick={() => fileInputRef.current?.click()}
              className="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center cursor-pointer hover:border-orange-400 transition-colors"
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
                <div className="flex items-center justify-center gap-2 text-orange-600">
                  <FileText className="w-5 h-5" />
                  <span>{uploadedFile.name}</span>
                </div>
              ) : (
                <>
                  <p className="text-gray-600 mb-2">点击或拖放文件到这里上传</p>
                  <p className="text-sm text-gray-400">支持docx、doc、txt格式文件，文件大小不超过100M</p>
                </>
              )}
            </div>
          ) : (
            <textarea
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="请在此粘贴论文内容..."
              className="w-full h-64 p-4 border border-gray-200 rounded-xl resize-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          )}

          <div className="mt-6 flex justify-center">
            <button
              onClick={handleGenerate}
              disabled={processing}
              className="bg-gradient-to-r from-orange-500 to-red-500 text-white px-8 py-3 rounded-xl font-medium hover:shadow-lg transition-all text-lg flex items-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  生成中... {progress}%
                </>
              ) : (
                <>
                  <Sparkles className="w-5 h-5" />
                  开始生成PPT
                </>
              )}
            </button>
          </div>

          {processing && (
            <div className="mt-4">
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-gradient-to-r from-orange-500 to-red-500 transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          )}
        </div>

        {pptOutline && (
          <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">PPT大纲预览</h3>
              <button
                onClick={handleDownloadPptx}
                className="bg-gradient-to-r from-orange-500 to-red-500 text-white px-4 py-2 rounded-lg font-medium hover:shadow-lg transition-all flex items-center gap-2"
              >
                <Download className="w-4 h-4" />
                导出PPTX
              </button>
            </div>
            <div className="p-4 bg-gray-50 rounded-xl max-h-96 overflow-y-auto">
              <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">{pptOutline}</pre>
            </div>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                <Presentation className="w-5 h-5 text-orange-600" />
              </div>
              <div>
                <h4 className="font-medium text-gray-800">专业模板</h4>
                <p className="text-xs text-gray-500">精美答辩风格</p>
              </div>
            </div>
            <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-red-600" />
              </div>
              <div>
                <h4 className="font-medium text-gray-800">智能提炼</h4>
                <p className="text-xs text-gray-500">自动提取要点</p>
              </div>
            </div>
            <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <Zap className="w-5 h-5 text-blue-600" />
              </div>
              <div>
                <h4 className="font-medium text-gray-800">一键生成</h4>
                <p className="text-xs text-gray-500">快速高效</p>
              </div>
            </div>
            <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Check className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <h4 className="font-medium text-gray-800">完美适配</h4>
                <p className="text-xs text-gray-500">答辩场景优化</p>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">使用说明</h3>
          <ul className="space-y-2 text-sm text-gray-600">
            <li className="flex items-start gap-2">
              <span className="text-orange-500 font-bold">1.</span>
              上传您的论文文件（支持docx、doc、txt格式）或直接粘贴论文内容
            </li>
            <li className="flex items-start gap-2">
              <span className="text-orange-500 font-bold">2.</span>
              AI将自动分析论文结构，提取核心要点
            </li>
            <li className="flex items-start gap-2">
              <span className="text-orange-500 font-bold">3.</span>
              生成专业的答辩PPT大纲，包含研究背景、方法、结果、结论等
            </li>
            <li className="flex items-start gap-2">
              <span className="text-orange-500 font-bold">4.</span>
              点击下载即可获得精美的PPTX文件，可直接用于答辩
            </li>
          </ul>
        </div>
      </div>
    </main>
  );
}

export default PptPage;
