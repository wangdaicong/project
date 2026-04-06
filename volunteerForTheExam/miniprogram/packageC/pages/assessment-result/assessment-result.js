const request = require('../../utils/request');

Page({
  data: {
    recordId: null,
    categoryScores: {},
    recommendedMajors: [],
    totalQuestions: 0,
    loading: true
  },

  onLoad(options) {
    if (options.recordId) {
      this.setData({ recordId: options.recordId });
      this.loadResult();
    }
  },

  // 加载测评结果
  loadResult() {
    wx.showLoading({ title: '加载中...' });
    
    request.get(`/assessment/record/${this.data.recordId}`).then(res => {
      console.log('测评记录:', res);
      
      // 解析JSON数据
      const categoryScores = JSON.parse(res.resultScores || '{}');
      const recommendedMajors = JSON.parse(res.recommendedMajors || '[]');
      
      this.setData({
        categoryScores,
        recommendedMajors,
        loading: false
      });
      
      wx.hideLoading();
    }).catch(err => {
      console.error('加载失败:', err);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  // 查看专业详情
  viewMajorDetail(e) {
    const majorName = e.currentTarget.dataset.name;
    wx.showToast({
      title: `查看${majorName}详情`,
      icon: 'none'
    });
    // TODO: 跳转到专业详情页
  },

  // 重新测评
  retakeAssessment() {
    wx.redirectTo({
      url: '/pages/assessment/assessment'
    });
  },

  // 返回首页
  goHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
