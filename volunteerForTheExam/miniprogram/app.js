App({
  globalData: {
    userInfo: null,
    userScore: null,
    userProvince: null,
    compareList: [],
    baseUrl: 'http://localhost:8080/api',
    apiUrl: 'http://localhost:8080',
    isRecommendMode: false
  },

  onLaunch() {
    const score = wx.getStorageSync('userScore');
    const province = wx.getStorageSync('userProvince');
    if (score) this.globalData.userScore = score;
    if (province) this.globalData.userProvince = province;
  },

  setUserInfo(score, province) {
    this.globalData.userScore = score;
    this.globalData.userProvince = province;
    wx.setStorageSync('userScore', score);
    wx.setStorageSync('userProvince', province);
  },

  addToCompare(item) {
    const list = this.globalData.compareList;
    const exists = list.find(i => i.id === item.id);
    if (!exists && list.length < 5) {
      list.push(item);
      wx.showToast({
        title: '已加入对比',
        icon: 'success'
      });
      return true;
    } else if (list.length >= 5) {
      wx.showToast({
        title: '最多对比5个',
        icon: 'none'
      });
      return false;
    }
    return false;
  },

  removeFromCompare(id) {
    const index = this.globalData.compareList.findIndex(i => i.id === id);
    if (index > -1) {
      this.globalData.compareList.splice(index, 1);
    }
  }
})
