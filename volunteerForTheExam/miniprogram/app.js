App({
  globalData: {
    userInfo: null,
    userId: null,
    userScore: null,
    userProvince: null,
    compareList: [],
    baseUrl: 'http://localhost:8083/api',
    apiUrl: 'http://localhost:8083',
    isRecommendMode: false
  },

  onLaunch() {
    // 加载用户信息
    const userId = wx.getStorageSync('userId');
    const userInfo = wx.getStorageSync('userInfo');
    const score = wx.getStorageSync('userScore');
    const province = wx.getStorageSync('userProvince');
    
    if (userId) this.globalData.userId = userId;
    if (userInfo) this.globalData.userInfo = userInfo;
    if (score) this.globalData.userScore = score;
    if (province) this.globalData.userProvince = province;
    
    // 自动登录
    this.checkLogin();
  },

  // 检查登录状态
  checkLogin() {
    const userId = this.globalData.userId;
    if (!userId) {
      // 未登录，尝试静默登录
      this.wxLogin();
    }
  },

  // 微信登录
  wxLogin() {
    wx.login({
      success: (res) => {
        if (res.code) {
          // 发送code到后端
          wx.request({
            url: this.globalData.baseUrl + '/user/login',
            method: 'POST',
            data: { code: res.code },
            success: (response) => {
              if (response.data.code === 200) {
                const data = response.data.data;
                this.globalData.userId = data.userId;
                this.globalData.userInfo = {
                  openid: data.openid,
                  nickname: data.nickname,
                  avatar: data.avatar
                };
                
                // 保存到本地
                wx.setStorageSync('userId', data.userId);
                wx.setStorageSync('userInfo', this.globalData.userInfo);
                
                console.log('登录成功，userId:', data.userId);
              }
            }
          });
        }
      }
    });
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
