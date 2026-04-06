const request = require('../../utils/request');

Page({
  data: {
    applicationId: null,
    application: null,
    volunteers: [],
    analysis: null,
    loading: true
  },

  onLoad(options) {
    if (options.applicationId) {
      this.setData({ applicationId: options.applicationId });
      this.loadResult();
    }
  },

  async loadResult() {
    wx.showLoading({ title: '加载中...' });
    
    try {
      const res = await request.get(`/volunteer/detail/${this.data.applicationId}`);
      
      if (res.code === 200) {
        this.setData({
          application: res.data.application,
          volunteers: res.data.volunteers || [],
          analysis: res.data.analysis,
          loading: false
        });
      } else {
        wx.showToast({ title: res.message, icon: 'none' });
      }
    } catch (error) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  getRiskLevelText(level) {
    const map = {
      'rush': '冲刺',
      'stable': '稳妥',
      'safe': '保底'
    };
    return map[level] || level;
  },

  getProbabilityText(prob) {
    const map = {
      'high': '高',
      'medium': '中',
      'low': '低',
      'unknown': '未知'
    };
    return map[prob] || prob;
  },

  viewUniversityDetail(e) {
    const { universityid } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/packageB/pages/university/detail?id=${universityid}`
    });
  },

  backToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  reEdit() {
    wx.navigateTo({
      url: `/pages/volunteer/volunteer?applicationId=${this.data.applicationId}`
    });
  },

  shareResult() {
    wx.showToast({ title: '分享功能开发中', icon: 'none' });
  }
});
