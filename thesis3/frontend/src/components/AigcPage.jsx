import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Upload, FileText, Loader2, Download, Copy, Check, Sparkles, Shield, Zap, Clock, Trash2, X, File, GitCompare } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '../api';

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

function AigcPage() {
  const [language, setLanguage] = useState('zh');
  const [submitMode, setSubmitMode] = useState('paste');
  const [inputText, setInputText] = useState('');
  const [outputText, setOutputText] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [copied, setCopied] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [activeSegments, setActiveSegments] = useState({});
  const [activeStage, setActiveStage] = useState('polish');
  const [activeStatus, setActiveStatus] = useState('');
  const [currentPosition, setCurrentPosition] = useState(0);
  const [totalSegments, setTotalSegments] = useState(0);
  const [stageView, setStageView] = useState('enhance');
  const [segmentsList, setSegmentsList] = useState([]);
  const [segmentsLoading, setSegmentsLoading] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [historySession, setHistorySession] = useState(null);
  const [historySegments, setHistorySegments] = useState([]);
  const [historyChanges, setHistoryChanges] = useState([]);
  const [historyTab, setHistoryTab] = useState('result');
  const [historyLoading, setHistoryLoading] = useState(false);
  const fileInputRef = useRef(null);
  const dropZoneRef = useRef(null);
  const eventSourceRef = useRef(null);
  const progressPollerRef = useRef(null);

  const currentStageLabel = useMemo(() => {
    if (activeStage === 'enhance') return '原创性增强';
    return '论文润色';
  }, [activeStage]);

  useEffect(() => {
    let cancelled = false;
    api.get('/optimization/sessions')
      .then(resp => {
        if (cancelled) return;
        if (resp?.data?.code === 200) {
          setSessions(Array.isArray(resp.data.data) ? resp.data.data : []);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      if (progressPollerRef.current) {
        clearInterval(progressPollerRef.current);
        progressPollerRef.current = null;
      }
    };
  }, []);

  const aggregatedOutput = useMemo(() => {
    const entries = Object.entries(activeSegments)
      .map(([k, v]) => ({ idx: Number(k), ...v }))
      .sort((a, b) => a.idx - b.idx);
    const stageKey = stageView === 'polish' ? 'polish' : 'enhance';
    return entries.map(e => (e[stageKey] || '')).join('\n\n').trim();
  }, [activeSegments, stageView]);

  useEffect(() => {
    if (activeStage === 'enhance') {
      setStageView('enhance');
    }
  }, [activeStage]);

  const refreshSegments = async (sessionId) => {
    if (!sessionId) return;
    setSegmentsLoading(true);
    try {
      const resp = await api.get(`/optimization/sessions/${sessionId}/segments`);
      if (resp?.data?.code === 200) {
        setSegmentsList(Array.isArray(resp.data.data) ? resp.data.data : []);
      } else {
        setSegmentsList([]);
      }
    } catch {
      setSegmentsList([]);
    } finally {
      setSegmentsLoading(false);
    }
  };

  const stopSession = async (sessionId) => {
    if (!sessionId) return;
    try {
      await api.post(`/optimization/sessions/${sessionId}/stop`);
      toast.success('已请求停止');
      const refreshed = await api.get('/optimization/sessions');
      if (refreshed?.data?.code === 200) {
        setSessions(Array.isArray(refreshed.data.data) ? refreshed.data.data : []);
      }
      if (historyOpen && historySession?.sessionId === sessionId) {
        const latest = await refreshProgress(sessionId);
        if (latest) setHistorySession(latest);
      }
      if (activeSession?.sessionId === sessionId) {
        const latest = await refreshProgress(sessionId);
        if (latest) {
          setActiveStatus(latest.status || '');
          setActiveStage(latest.currentStage || 'polish');
          setProgress(typeof latest.progress === 'number' ? Math.round(latest.progress) : 0);
          setCurrentPosition(typeof latest.currentPosition === 'number' ? latest.currentPosition : 0);
          setTotalSegments(typeof latest.totalSegments === 'number' ? latest.totalSegments : 0);
        }
      }
    } catch {
      toast.error('停止失败');
    }
  };

  const refreshProgress = async (sessionId) => {
    if (!sessionId) return null;
    try {
      const resp = await api.get(`/optimization/sessions/${sessionId}/progress`);
      if (resp?.data?.code === 200 && resp.data.data) {
        return resp.data.data;
      }
    } catch {
      // ignore
    }
    return null;
  };

  const formatDate = (dt) => {
    if (!dt) return '';
    const d = new Date(dt);
    if (Number.isNaN(d.getTime())) return '';
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}/${m}/${day}`;
  };

  const statusMeta = (status) => {
    if (status === 'completed') {
      return { label: '已完成', icon: Check, iconClass: 'text-green-600', dotClass: 'bg-green-500' };
    }
    if (status === 'failed') {
      return { label: '失败', icon: X, iconClass: 'text-red-600', dotClass: 'bg-red-500' };
    }
    if (status === 'stopped') {
      return { label: '已停止', icon: X, iconClass: 'text-gray-500', dotClass: 'bg-gray-400' };
    }
    if (status === 'processing') {
      return { label: '处理中', icon: Loader2, iconClass: 'text-blue-600', dotClass: 'bg-blue-500' };
    }
    if (status === 'queued') {
      return { label: '排队中', icon: Clock, iconClass: 'text-amber-600', dotClass: 'bg-amber-500' };
    }
    return { label: status || '-', icon: Clock, iconClass: 'text-gray-500', dotClass: 'bg-gray-400' };
  };

  const openHistory = async (s) => {
    if (!s?.sessionId) return;
    setHistoryOpen(true);
    setHistoryTab('result');
    setHistorySession(null);
    setHistorySegments([]);
    setHistoryChanges([]);
    setHistoryLoading(true);
    try {
      const [p, seg, ch] = await Promise.all([
        refreshProgress(s.sessionId),
        api.get(`/optimization/sessions/${s.sessionId}/segments`).catch(() => null),
        api.get(`/optimization/sessions/${s.sessionId}/changes`).catch(() => null)
      ]);

      if (p) setHistorySession(p);
      if (seg?.data?.code === 200) {
        setHistorySegments(Array.isArray(seg.data.data) ? seg.data.data : []);
      }
      if (ch?.data?.code === 200) {
        setHistoryChanges(Array.isArray(ch.data.data) ? ch.data.data : []);
      }
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleDeleteSession = async (sessionId) => {
    if (!sessionId) return;
    try {
      const resp = await api.delete(`/optimization/sessions/${sessionId}`);
      if (resp?.data?.code === 200) {
        toast.success('已删除');
        const refreshed = await api.get('/optimization/sessions');
        if (refreshed?.data?.code === 200) {
          setSessions(Array.isArray(refreshed.data.data) ? refreshed.data.data : []);
        }
        if (historySession?.sessionId === sessionId) {
          setHistoryOpen(false);
        }
      } else {
        const backendCode = resp?.data?.code;
        const backendMsg = resp?.data?.message;
        toast.error(`删除失败${backendCode ? ` (code ${backendCode})` : ''}${backendMsg ? `：${backendMsg}` : ''}`);
      }
    } catch (err) {
      if (err?.response?.status === 405) {
        try {
          const resp2 = await api.post(`/optimization/sessions/${sessionId}/delete`);
          if (resp2?.data?.code === 200) {
            toast.success('已删除');
            const refreshed = await api.get('/optimization/sessions');
            if (refreshed?.data?.code === 200) {
              setSessions(Array.isArray(refreshed.data.data) ? refreshed.data.data : []);
            }
            if (historySession?.sessionId === sessionId) {
              setHistoryOpen(false);
            }
            return;
          }
          const backendCode2 = resp2?.data?.code;
          const backendMsg2 = resp2?.data?.message;
          toast.error(`删除失败${backendCode2 ? ` (code ${backendCode2})` : ''}${backendMsg2 ? `：${backendMsg2}` : ''}`);
          return;
        } catch (err2) {
          const status2 = err2?.response?.status;
          const backendCode2 = err2?.response?.data?.code;
          const backendMsg2 = err2?.response?.data?.message;
          const msg2 = err2?.message;
          toast.error(
            `删除失败${status2 ? ` (HTTP ${status2})` : ''}${backendCode2 ? ` (code ${backendCode2})` : ''}`
            + `${backendMsg2 ? `：${backendMsg2}` : msg2 ? `：${msg2}` : ''}`
          );
          return;
        }
      }
      const status = err?.response?.status;
      const backendCode = err?.response?.data?.code;
      const backendMsg = err?.response?.data?.message;
      const msg = err?.message;
      toast.error(
        `删除失败${status ? ` (HTTP ${status})` : ''}${backendCode ? ` (code ${backendCode})` : ''}`
        + `${backendMsg ? `：${backendMsg}` : msg ? `：${msg}` : ''}`
      );
    }
  };

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
    setActiveSegments({});
    setActiveStage('polish');
    setActiveStatus('queued');

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    try {
      const startResp = await api.post('/optimization/start', {
        originalText: textToProcess,
        processingMode: 'paper_polish_enhance'
      });

      if (startResp?.data?.code !== 200 || !startResp.data.data) {
        throw new Error(startResp?.data?.message || '启动失败');
      }

      const session = startResp.data.data;
      setActiveSession(session);
      setSessions(prev => [session, ...prev]);
      setSegmentsList([]);

      if (progressPollerRef.current) {
        clearInterval(progressPollerRef.current);
        progressPollerRef.current = null;
      }

      progressPollerRef.current = setInterval(async () => {
        const latest = await refreshProgress(session.sessionId);
        if (!latest) return;
        setActiveStatus(latest.status || '');
        setActiveStage(latest.currentStage || 'polish');
        setProgress(typeof latest.progress === 'number' ? Math.round(latest.progress) : 0);
        setCurrentPosition(typeof latest.currentPosition === 'number' ? latest.currentPosition : 0);
        setTotalSegments(typeof latest.totalSegments === 'number' ? latest.totalSegments : 0);
        if (['completed', 'failed', 'stopped'].includes(latest.status)) {
          clearInterval(progressPollerRef.current);
          progressPollerRef.current = null;
          setProcessing(false);
          refreshSegments(session.sessionId);
        }
      }, 800);

      const es = new EventSource(`/api/optimization/sessions/${session.sessionId}/stream`);
      eventSourceRef.current = es;

      es.onmessage = (evt) => {
        if (!evt?.data) return;
        let payload;
        try {
          payload = JSON.parse(evt.data);
        } catch {
          return;
        }

        const type = payload.type;
        if (type === 'progress') {
          const p = typeof payload.progress === 'number' ? payload.progress : 0;
          setProgress(Math.max(0, Math.min(100, Math.round(p))));
          setActiveStage(prev => payload.current_stage || prev);
          setActiveStatus(prev => payload.status || prev || 'processing');
          if (typeof payload.current_position === 'number') {
            setCurrentPosition(payload.current_position);
          }
          if (typeof payload.total_segments === 'number') {
            setTotalSegments(payload.total_segments);
          }
          return;
        }

        if (type === 'stage_started') {
          setActiveStage(prev => payload.stage || prev || 'polish');
          return;
        }

        if (type === 'content') {
          const segIndex = payload.segment_index;
          const stage = payload.stage || 'polish';
          const content = payload.content || '';
          if (segIndex === undefined || segIndex === null) return;

          setActiveSegments(prev => {
            const cur = prev[segIndex] || { polish: '', enhance: '' };
            const next = { ...prev };
            if (stage === 'enhance') {
              next[segIndex] = { ...cur, enhance: (cur.enhance || '') + content };
            } else {
              next[segIndex] = { ...cur, polish: (cur.polish || '') + content };
            }
            return next;
          });
          return;
        }

        if (type === 'history_compressed') {
          return;
        }

        if (type === 'error') {
          setActiveStatus('failed');
          toast.error(payload.message || '处理失败');
          setProcessing(false);
          if (progressPollerRef.current) {
            clearInterval(progressPollerRef.current);
            progressPollerRef.current = null;
          }
          if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
          }
          return;
        }

        if (type === 'stopped') {
          setActiveStatus('stopped');
          toast.success('已停止');
          setProcessing(false);
          if (progressPollerRef.current) {
            clearInterval(progressPollerRef.current);
            progressPollerRef.current = null;
          }
          if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
          }
          return;
        }

        if (type === 'completed') {
          setActiveStatus('completed');
          setProgress(100);
          toast.success('处理完成！');
          setProcessing(false);
          refreshSegments(session.sessionId);
          if (progressPollerRef.current) {
            clearInterval(progressPollerRef.current);
            progressPollerRef.current = null;
          }
          if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
          }
          return;
        }
      };

      es.onerror = () => {
        es.close();
        if (eventSourceRef.current === es) {
          eventSourceRef.current = null;
        }
      };

    } catch (error) {
      toast.error(error?.message || '启动失败，请重试');
      setProcessing(false);
    }
  };

  useEffect(() => {
    setOutputText(aggregatedOutput);
  }, [aggregatedOutput]);

  const handleStop = async () => {
    if (!activeSession?.sessionId) return;
    await stopSession(activeSession.sessionId);
  };

  const handleRetry = async () => {
    if (!activeSession?.sessionId) return;
    try {
      await api.post(`/optimization/sessions/${activeSession.sessionId}/retry`);
      toast.success('已重新排队');
      setProcessing(true);
    } catch {
      toast.error('重试失败');
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
    if (!activeSession?.sessionId) {
      toast.error('请先完成一次处理');
      return;
    }
    fetch(`/api/optimization/sessions/${activeSession.sessionId}/export`)
      .then(r => {
        if (!r.ok) throw new Error('导出失败');
        return r.blob();
      })
      .then(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `降AIGC结果_${activeSession.sessionId}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      })
      .catch(() => toast.error('导出失败'));
  };

  return (
    <main className="flex-1 p-6 overflow-y-auto">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-full bg-white rounded-2xl shadow-sm px-8 py-6">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
              强力降论文AIGC痕迹|查重率
            </h1>
            <p className="text-gray-700 font-medium">疑似度降低 60%</p>
            <p className="text-lg text-gray-800 mt-2">一键降低 论文aigc率 ai痕迹 查重率</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="mb-4 text-sm text-gray-500">
            适用于主流查AIGC平台(实时监测)
          </div>
          
          <div className="text-sm text-gray-600 mb-6 p-4 bg-blue-50 rounded-lg">
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
                        ? 'bg-blue-500 text-white'
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
                        ? 'bg-blue-500 text-white'
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
              className="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center cursor-pointer hover:border-blue-400 transition-colors"
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
                <div className="flex items-center justify-center gap-2 text-blue-600">
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
              placeholder="请在此粘贴需要降低AIGC率的论文内容..."
              className="w-full h-64 p-4 border border-gray-200 rounded-xl resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          )}

          <div className="mt-6 flex justify-center">
            <button
              onClick={handleProcess}
              disabled={processing}
              className="btn-primary px-8 py-3 text-lg flex items-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  处理中... {progress}%
                </>
              ) : (
                <>
                  <Sparkles className="w-5 h-5" />
                  开始降AIGC率
                </>
              )}
            </button>
          </div>

          {processing && (
            <div className="mt-4">
              <div className="flex items-center justify-between mb-2">
                <div className="text-sm text-gray-700">
                  当前阶段：{currentStageLabel}
                </div>
                <div className="text-sm text-gray-700">
                  进度：{Math.min(currentPosition + 1, Math.max(totalSegments, 1))} / {Math.max(totalSegments, 1)} 段
                </div>
              </div>
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-gradient-to-r from-blue-500 to-purple-500 transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          )}
        </div>

        {outputText && (
          <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">处理结果</h3>
              <div className="flex gap-2">
                <div className="flex gap-1">
                  <button
                    onClick={() => setStageView('polish')}
                    className={`px-3 py-2 rounded-lg text-sm transition-all ${stageView === 'polish' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
                    disabled={processing}
                  >
                    润色
                  </button>
                  <button
                    onClick={() => setStageView('enhance')}
                    className={`px-3 py-2 rounded-lg text-sm transition-all ${stageView === 'enhance' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
                    disabled={processing}
                  >
                    增强
                  </button>
                </div>
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

        {activeSession?.sessionId && (
          <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">分段结果</h3>
              <button
                onClick={() => refreshSegments(activeSession.sessionId)}
                className="btn-secondary"
                disabled={segmentsLoading}
              >
                {segmentsLoading ? '加载中...' : '刷新'}
              </button>
            </div>
            {segmentsList.length === 0 ? (
              <div className="text-sm text-gray-500">暂无分段结果</div>
            ) : (
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {segmentsList.map(seg => {
                  const text = stageView === 'polish'
                    ? (seg.polishedText || seg.originalText || '')
                    : (seg.enhancedText || seg.polishedText || seg.originalText || '');
                  return (
                    <div key={seg.id} className="p-4 bg-gray-50 rounded-xl">
                      <div className="text-xs text-gray-500 mb-2">段落 {Number(seg.segmentIndex) + 1} ｜ {seg.status}</div>
                      <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">{text}</pre>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {sessions.length > 0 && (
          <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <Clock className="w-5 h-5 text-gray-600" />
                历史记录
              </h3>
              <button
                onClick={async () => {
                  try {
                    const resp = await api.get('/optimization/sessions');
                    if (resp?.data?.code === 200) {
                      setSessions(Array.isArray(resp.data.data) ? resp.data.data : []);
                    }
                  } catch {
                    toast.error('刷新失败');
                  }
                }}
                className="btn-secondary"
                disabled={processing}
              >
                刷新
              </button>
            </div>
            <div className="space-y-3 max-h-80 overflow-y-auto">
              {sessions
                .slice()
                .sort((a, b) => {
                  const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0;
                  const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0;
                  return tb - ta;
                })
                .slice(0, 50)
                .map(s => {
                  const meta = statusMeta(s.status);
                  const Icon = meta.icon;
                  const isActive = activeSession?.sessionId === s.sessionId;
                  const deletable = s.status !== 'processing' && s.status !== 'queued';
                  const stoppable = s.status === 'processing' || s.status === 'queued';
                  return (
                    <button
                      key={s.id}
                      className={`w-full text-left px-4 py-4 rounded-2xl border transition-all ${
                        isActive ? 'border-blue-400 bg-blue-50' : 'border-gray-100 hover:bg-gray-50'
                      }`}
                      onClick={() => openHistory(s)}
                    >
                      <div className="flex items-center justify-between gap-4">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className={`w-10 h-10 rounded-full bg-white border flex items-center justify-center ${meta.iconClass}`}
                          >
                            <Icon className={`w-5 h-5 ${s.status === 'processing' ? 'animate-spin' : ''}`} />
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-2">
                              <div className="text-base font-semibold text-gray-900">{meta.label}</div>
                              <div className={`w-2 h-2 rounded-full ${meta.dotClass}`} />
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-4">
                          <div className="text-sm text-gray-400">{formatDate(s.completedAt || s.updatedAt || s.createdAt)}</div>
                          {stoppable && (
                            <button
                              className="px-3 py-2 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 text-sm"
                              onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                stopSession(s.sessionId);
                              }}
                              title="停止"
                              aria-label="停止"
                            >
                              停止
                            </button>
                          )}
                          {deletable && (
                            <button
                              className="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600"
                              onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                handleDeleteSession(s.sessionId);
                              }}
                              title="删除"
                              aria-label="删除"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </div>
                      <div className="mt-3 text-sm text-gray-500">...</div>
                    </button>
                  );
                })}
            </div>
          </div>
        )}

        {historyOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onMouseDown={() => setHistoryOpen(false)}>
            <div
              className="w-full max-w-6xl bg-white rounded-2xl shadow-2xl overflow-hidden"
              onMouseDown={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-white">
                <div className="flex items-center gap-3">
                  <button className="text-blue-600 hover:text-blue-700" onClick={() => setHistoryOpen(false)}>
                    <span className="text-sm">返回</span>
                  </button>
                  <div className="h-4 w-px bg-gray-200" />
                  <div className="flex items-baseline gap-3">
                    <div className="text-lg font-semibold text-gray-900">会话详情</div>
                    {historySession?.status && (
                      <div className="text-sm text-gray-400">{formatDate(historySession.completedAt || historySession.updatedAt || historySession.createdAt)}</div>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  {historySession?.status === 'completed' ? (
                    <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-green-50 text-green-700 text-sm font-semibold">
                      <Check className="w-4 h-4" />
                      已完成
                    </div>
                  ) : (
                    <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-gray-100 text-gray-700 text-sm font-medium">
                      <span className="w-2 h-2 rounded-full bg-gray-400" />
                      {statusMeta(historySession?.status).label}
                    </div>
                  )}
                  {(historySession?.status === 'processing' || historySession?.status === 'queued') && (
                    <button
                      onClick={() => stopSession(historySession.sessionId)}
                      className="btn-secondary"
                      disabled={!historySession?.sessionId}
                    >
                      停止
                    </button>
                  )}
                  <button
                    onClick={() => {
                      const sid = historySession?.sessionId;
                      if (!sid) return;
                      fetch(`/api/optimization/sessions/${sid}/export`).then(async (r) => {
                        if (!r.ok) throw new Error('下载失败');
                        const blob = await r.blob();
                        const url = URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = `optimization_${sid}.txt`;
                        document.body.appendChild(a);
                        a.click();
                        a.remove();
                        URL.revokeObjectURL(url);
                      }).catch(() => toast.error('导出失败'));
                    }}
                    className="inline-flex items-center gap-2 px-5 py-2 rounded-full bg-blue-600 text-white font-semibold hover:bg-blue-700 transition-colors"
                    disabled={!historySession?.sessionId}
                  >
                    <Download className="w-4 h-4" />
                    导出
                  </button>
                  <button className="p-2 rounded-lg hover:bg-gray-100 text-gray-500" onClick={() => setHistoryOpen(false)}>
                    <X className="w-5 h-5" />
                  </button>
                </div>
              </div>

              <div className="bg-gray-50 px-6 pt-4 pb-6">
                <div className="flex justify-center">
                  <div className="inline-flex rounded-2xl bg-white p-1 shadow-sm border border-gray-100">
                  <button
                    className={`px-5 py-2.5 rounded-2xl text-sm font-medium transition-all flex items-center gap-2 ${historyTab === 'result' ? 'bg-gray-900 text-white shadow' : 'text-gray-500 hover:text-gray-700'}`}
                    onClick={() => setHistoryTab('result')}
                  >
                    <File className="w-4 h-4" />
                    优化结果
                  </button>
                  <button
                    className={`px-5 py-2.5 rounded-2xl text-sm font-medium transition-all flex items-center gap-2 ${historyTab === 'changes' ? 'bg-gray-900 text-white shadow' : 'text-gray-500 hover:text-gray-700'}`}
                    onClick={() => setHistoryTab('changes')}
                  >
                    <GitCompare className="w-4 h-4" />
                    变更对照
                  </button>
                  </div>
                </div>

                {historyLoading ? (
                  <div className="flex items-center justify-center py-16 text-gray-500">
                    <Loader2 className="w-5 h-5 animate-spin mr-2" />
                    加载中...
                  </div>
                ) : (
                  <>
                    {historyTab === 'result' && (
                      <div className="mt-5 grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
                          <div className="flex items-center gap-3 mb-3">
                            <div className="text-sm font-semibold text-gray-800">增强后的文本</div>
                            <div className="flex gap-1">
                              <button
                                onClick={() => setStageView('polish')}
                                className={`px-3 py-1.5 rounded-lg text-xs transition-all ${stageView === 'polish' ? 'bg-blue-500 text-white' : 'bg-white text-gray-600 hover:bg-gray-100'}`}
                              >
                                润色
                              </button>
                              <button
                                onClick={() => setStageView('enhance')}
                                className={`px-3 py-1.5 rounded-lg text-xs transition-all ${stageView === 'enhance' ? 'bg-blue-500 text-white' : 'bg-white text-gray-600 hover:bg-gray-100'}`}
                              >
                                增强
                              </button>
                            </div>
                          </div>
                          <div className="max-h-[60vh] overflow-y-auto">
                            <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">
                              {historySegments
                                .slice()
                                .sort((a, b) => Number(a.segmentIndex) - Number(b.segmentIndex))
                                .map(seg => {
                                  if (stageView === 'polish') {
                                    return seg.polishedText || seg.originalText || '';
                                  }
                                  return seg.enhancedText || seg.polishedText || seg.originalText || '';
                                })
                                .join('\n\n')
                                .trim()}
                            </pre>
                          </div>
                        </div>

                        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
                          <div className="text-sm font-semibold text-gray-800 mb-3">原始文本</div>
                          <div className="max-h-[60vh] overflow-y-auto">
                            <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">{historySession?.originalText || ''}</pre>
                          </div>
                        </div>
                      </div>
                    )}

                    {historyTab === 'changes' && (
                      <div className="mt-5">
                        <div className="text-base font-semibold text-gray-900 mb-4">变更对照记录</div>
                        <div className="space-y-4 max-h-[65vh] overflow-y-auto pr-1">
                        {historyChanges.length === 0 ? (
                          <div className="text-sm text-gray-500">暂无变更记录</div>
                        ) : (
                          historyChanges.map((c) => (
                            <div key={c.id} className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
                              <div className="flex items-center gap-2 mb-4">
                                <span className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-blue-50 text-blue-700">
                                  段落 {Number(c.segmentIndex) + 1}
                                </span>
                                <span className={`px-2.5 py-1 rounded-lg text-xs font-semibold ${c.stage === 'enhance' ? 'bg-purple-50 text-purple-700' : 'bg-sky-50 text-sky-700'}`}>
                                  {c.stage === 'enhance' ? '增强' : '润色'}
                                </span>
                              </div>
                              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="rounded-2xl border border-red-100 bg-red-50/40 p-4">
                                  <div className="text-sm font-semibold text-gray-700 mb-3">修改前</div>
                                  <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">{c.beforeText || ''}</pre>
                                </div>
                                <div className="rounded-2xl border border-green-100 bg-green-50/40 p-4">
                                  <div className="text-sm font-semibold text-gray-700 mb-3">修改后</div>
                                  <pre className="whitespace-pre-wrap text-gray-700 font-sans text-sm">{c.afterText || ''}</pre>
                                </div>
                              </div>
                            </div>
                          ))
                        )}
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">使用说明</h3>
          <ul className="space-y-2 text-sm text-gray-600">
            <li className="flex items-start gap-2">
              <span className="text-blue-500 font-bold">1.</span>
              本站降重和降aigc适用于主流查重平台；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-500 font-bold">2.</span>
              直接上传原文件效果最佳；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-500 font-bold">3.</span>
              支持中文、英语、日语、韩语、俄语等语言；
            </li>
            <li className="flex items-start gap-2">
              <span className="text-blue-500 font-bold">4.</span>
              安全保障：HTTPS加密传输，检测记录不留存，符合GDPR及学术伦理规范；
            </li>
          </ul>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
              <Shield className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h4 className="font-medium text-gray-800">安全护航</h4>
              <p className="text-xs text-gray-500">银行级加密保障</p>
            </div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
              <Check className="w-5 h-5 text-green-600" />
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
              <h4 className="font-medium text-blue-600 mb-2">Q【安全护航】值得信任</h4>
              <p className="text-sm text-gray-600">已为200万用户守护创作成果，银行级加密技术保障数据安全，检测全程可随时销毁记录，放心查重零风险！</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-blue-600 mb-2">Q【隐私保障】查重后会收录我的论文吗？</h4>
              <p className="text-sm text-gray-600">绝对不收录！系统自动粉碎记录，也支持下载后永久删除，泄密我们承担法律责任！</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-blue-600 mb-2">Q【透明收费】请问是怎么计费的？</h4>
              <p className="text-sm text-gray-600">按字符数（不含空格/标点）计费！系统智能统计字符，无任何隐藏收费。</p>
            </div>
            <div className="border-b border-gray-100 pb-4">
              <h4 className="font-medium text-blue-600 mb-2">Q【大文件处理】文档超过20MB怎么办？</h4>
              <p className="text-sm text-gray-600">删除图片后再上传文件，或者直接粘贴word文本内容进行降重</p>
            </div>
            <div>
              <h4 className="font-medium text-blue-600 mb-2">Q【多种方式】无法上传论文？</h4>
              <p className="text-sm text-gray-600">推荐「粘贴文本模式」：Ctrl+A全选文字，30秒完成提交，检测结果完全一致！</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default AigcPage;
