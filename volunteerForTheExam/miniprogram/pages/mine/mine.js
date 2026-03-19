const app = getApp();

Page({
  data: {
    hasScore: false,
    userScore: null,
    userProvince: null,
    compareCount: 0,
    favoriteCount: 0,
    viewCount: 0
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
    this.setData({
      compareCount: compareList.length,
      favoriteCount: wx.getStorageSync('favoriteCount') || 0,
      viewCount: wx.getStorageSync('viewCount') || 0
    });
  },

  goToHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  goToCompare() {
    wx.switchTab({
      url: '/pages/compare/compare'
    });
  },

  goToFavorite() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
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
