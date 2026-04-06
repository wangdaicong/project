const app = getApp();
const request = require('../../utils/request');

Page({
  data: {
    hasScore: false,
    userScore: null,
    userProvince: null,
    compareCount: 0,
    favoriteCount: 0
  },

  onShow() {
    this.loadUserInfo();
    this.loadStats();
  },


  loadUserInfo() {
    const score = app.globalData.userScore;
    const province = app.globalData.userProvince;
    
    this.setData({
      hasScore: !!score,
      userScore: score,
      userProvince: province
    });
  },

  loadStats() {
    const compareList = app.globalData.compareList || [];
    const favorites = wx.getStorageSync('favorites') || [];
    this.setData({
      compareCount: compareList.length,
      favoriteCount: favorites.length
    });
  },

  goToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  goToCompare() {
    wx.navigateTo({
      url: '/pages/compare/compare'
    });
  },

  goToFavorite() {
    wx.navigateTo({
      url: '/pages/favorite/favorite'
    });
  },

  goToHistory() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  goToAbout() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  goToFeedback() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  }
});
