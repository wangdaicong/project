// pages/ai-chat/ai-chat.js
Page({
  data: {
    messages: [],
    inputText: '',
    scrollIntoView: '',
    commonQuestions: [],
    userInfo: {
      score: 0,
      province: '',
      category: ''
    },
    showContextPanel: false
  },

  onLoad(options) {
    // 加载常见问题
    this.loadCommonQuestions();
    
    // 添加欢迎消息
    this.addMessage({
      type: 'ai',
      content: '您好！我是志愿填报智能助手，我可以帮您：\n\n1. 推荐适合的院校和专业\n2. 解答志愿填报相关问题\n3. 分析历年录取数据\n4. 提供专业就业信息\n5. 指导志愿填报策略\n\n请告诉我您的具体问题，我会尽力为您解答。',
      time: this.formatTime(new Date())
    });
  },

  // 加载常见问题
  loadCommonQuestions() {
    wx.request({
      url: 'http://localhost:8080/api/ai/common-questions',
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            commonQuestions: res.data.data
          });
        }
      }
    });
  },

  // 输入框内容变化
  onInputChange(e) {
    this.setData({
      inputText: e.detail.value
    });
  },

  // 发送消息
  sendMessage() {
    const { inputText, userInfo } = this.data;
    
    if (!inputText.trim()) {
      wx.showToast({
        title: '请输入问题',
        icon: 'none'
      });
      return;
    }

    // 添加用户消息
    this.addMessage({
      type: 'user',
      content: inputText,
      time: this.formatTime(new Date())
    });

    // 清空输入框
    this.setData({
      inputText: ''
    });

    // 显示加载中
    this.addMessage({
      type: 'ai',
      content: '正在思考中...',
      time: this.formatTime(new Date()),
      loading: true
    });

    // 调用AI接口
    wx.request({
      url: 'http://localhost:8080/api/ai/chat',
      method: 'POST',
      data: {
        question: inputText,
        context: {
          score: userInfo.score || undefined,
          province: userInfo.province || undefined,
          category: userInfo.category || undefined
        }
      },
      success: (res) => {
        // 移除加载消息
        const messages = this.data.messages.filter(msg => !msg.loading);
        
        if (res.data.code === 200) {
          // 添加AI回复
          messages.push({
            type: 'ai',
            content: res.data.data.answer,
            time: this.formatTime(new Date())
          });
        } else {
          messages.push({
            type: 'ai',
            content: '抱歉，我暂时无法回答这个问题，请稍后再试。',
            time: this.formatTime(new Date())
          });
        }
        
        this.setData({
          messages: messages
        }, () => {
          this.scrollToBottom();
        });
      },
      fail: () => {
        const messages = this.data.messages.filter(msg => !msg.loading);
        messages.push({
          type: 'ai',
          content: '网络连接失败，请检查网络后重试。',
          time: this.formatTime(new Date())
        });
        this.setData({ messages });
      }
    });
  },

  // 添加消息
  addMessage(message) {
    const messages = this.data.messages;
    message.id = 'msg-' + Date.now();
    messages.push(message);
    
    this.setData({
      messages: messages,
      scrollIntoView: message.id
    });
  },

  // 点击常见问题
  onQuestionTap(e) {
    const question = e.currentTarget.dataset.question;
    this.setData({
      inputText: question
    });
    this.sendMessage();
  },

  // 显示/隐藏上下文面板
  toggleContextPanel() {
    this.setData({
      showContextPanel: !this.data.showContextPanel
    });
  },

  // 更新用户信息
  onScoreInput(e) {
    this.setData({
      'userInfo.score': parseInt(e.detail.value) || 0
    });
  },

  onProvinceInput(e) {
    this.setData({
      'userInfo.province': e.detail.value
    });
  },

  onCategoryChange(e) {
    const categories = ['理科', '文科'];
    this.setData({
      'userInfo.category': categories[e.detail.value]
    });
  },

  // 保存上下文
  saveContext() {
    wx.showToast({
      title: '信息已保存',
      icon: 'success'
    });
    this.setData({
      showContextPanel: false
    });
  },

  // 清空对话
  clearMessages() {
    wx.showModal({
      title: '提示',
      content: '确定要清空所有对话记录吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            messages: []
          });
          this.onLoad();
        }
      }
    });
  },

  // 滚动到底部
  scrollToBottom() {
    const messages = this.data.messages;
    if (messages.length > 0) {
      this.setData({
        scrollIntoView: messages[messages.length - 1].id
      });
    }
  },

  // 格式化时间
  formatTime(date) {
    const hour = date.getHours();
    const minute = date.getMinutes();
    return `${hour < 10 ? '0' + hour : hour}:${minute < 10 ? '0' + minute : minute}`;
  }
});
