const request = require('../../utils/request');

Page({
  data: {
    questions: [],
    currentIndex: 0,
    answers: [],
    categories: ['兴趣', '能力', '性格', '价值观'],
    currentCategory: '',
    progress: 0,
    totalQuestions: 0,
    loading: true
  },

  onLoad() {
    this.loadQuestionnaire();
  },

  // 加载问卷
  loadQuestionnaire() {
    wx.showLoading({ title: '加载中...' });
    
    request.get('/assessment/questionnaire').then(res => {
      console.log('问卷数据:', res);
      
      this.setData({
        questions: res.questions || [],
        totalQuestions: res.total || 0,
        currentCategory: res.questions[0]?.category || '',
        loading: false
      });
      
      wx.hideLoading();
    }).catch(err => {
      console.error('加载问卷失败:', err);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  // 选择答案
  selectOption(e) {
    const optionId = e.currentTarget.dataset.id;
    const currentQuestion = this.data.questions[this.data.currentIndex];
    
    // 记录答案
    const answer = {
      questionId: currentQuestion.id,
      optionId: optionId,
      category: currentQuestion.category
    };
    
    const answers = [...this.data.answers];
    // 检查是否已回答过该问题
    const existIndex = answers.findIndex(a => a.questionId === currentQuestion.id);
    if (existIndex > -1) {
      answers[existIndex] = answer;
    } else {
      answers.push(answer);
    }
    
    this.setData({ answers });
    
    // 自动进入下一题
    setTimeout(() => {
      this.nextQuestion();
    }, 300);
  },

  // 下一题
  nextQuestion() {
    const nextIndex = this.data.currentIndex + 1;
    
    if (nextIndex < this.data.totalQuestions) {
      const nextQuestion = this.data.questions[nextIndex];
      this.setData({
        currentIndex: nextIndex,
        currentCategory: nextQuestion.category,
        progress: Math.round((nextIndex / this.data.totalQuestions) * 100)
      });
    } else {
      // 完成测评
      this.submitAssessment();
    }
  },

  // 上一题
  prevQuestion() {
    if (this.data.currentIndex > 0) {
      const prevIndex = this.data.currentIndex - 1;
      const prevQuestion = this.data.questions[prevIndex];
      this.setData({
        currentIndex: prevIndex,
        currentCategory: prevQuestion.category,
        progress: Math.round((prevIndex / this.data.totalQuestions) * 100)
      });
    }
  },

  // 提交测评
  submitAssessment() {
    if (this.data.answers.length < this.data.totalQuestions) {
      wx.showToast({
        title: '请完成所有问题',
        icon: 'none'
      });
      return;
    }
    
    wx.showLoading({ title: '分析中...' });
    
    const data = {
      answers: this.data.answers,
      userId: null // 暂不关联用户
    };
    
    request.post('/assessment/submit', data).then(res => {
      console.log('测评结果:', res);
      wx.hideLoading();
      
      // 跳转到结果页面
      wx.redirectTo({
        url: `/pages/assessment-result/assessment-result?recordId=${res.recordId}`
      });
    }).catch(err => {
      console.error('提交失败:', err);
      wx.hideLoading();
      wx.showToast({
        title: '提交失败',
        icon: 'none'
      });
    });
  },

  // 获取当前选中的答案
  getCurrentAnswer() {
    const currentQuestion = this.data.questions[this.data.currentIndex];
    const answer = this.data.answers.find(a => a.questionId === currentQuestion.id);
    return answer ? answer.optionId : null;
  }
});
