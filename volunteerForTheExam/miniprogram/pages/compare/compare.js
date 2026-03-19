const app = getApp();

Page({
  data: {
    compareList: [],
    showResult: false
  },

  onShow() {
    this.setData({
      compareList: app.globalData.compareList || []
    });
  },

  removeItem(e) {
    const id = e.currentTarget.dataset.id;
    app.removeFromCompare(id);
    this.setData({
      compareList: app.getCompareList() || []
    });
  },

  goToSearch() {
    wx.switchTab({
      url: '/pages/search/search'
    });
  }
})
