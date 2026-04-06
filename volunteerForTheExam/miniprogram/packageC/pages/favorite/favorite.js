const app = getApp();

Page({
  data: {
    favorites: [],
    isEmpty: true
  },

  onShow() {
    this.loadFavorites();
  },

  // 加载收藏列表
  loadFavorites() {
    const favorites = wx.getStorageSync('favorites') || [];
    
    // 按收藏时间倒序排列
    favorites.sort((a, b) => b.favoriteTime - a.favoriteTime);
    
    this.setData({
      favorites,
      isEmpty: favorites.length === 0
    });
  },

  // 取消收藏
  removeFavorite(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认取消',
      content: '确定要取消收藏这所院校吗？',
      success: (res) => {
        if (res.confirm) {
          let favorites = wx.getStorageSync('favorites') || [];
          favorites = favorites.filter(item => item.id !== id);
          
          wx.setStorageSync('favorites', favorites);
          wx.setStorageSync('favoriteCount', favorites.length);
          
          this.setData({
            favorites,
            isEmpty: favorites.length === 0
          });
          
          wx.showToast({
            title: '已取消收藏',
            icon: 'success'
          });
        }
      }
    });
  },

  // 跳转到院校详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/packageB/pages/university/detail?id=${id}`
    });
  },

  // 添加到对比
  addToCompare(e) {
    const id = e.currentTarget.dataset.id;
    const university = this.data.favorites.find(item => item.id === id);
    
    if (university) {
      const success = app.addToCompare(university);
      if (success) {
        wx.showToast({
          title: '已加入对比',
          icon: 'success'
        });
      }
    }
  },

  // 清空收藏
  clearAll() {
    if (this.data.favorites.length === 0) {
      return;
    }
    
    wx.showModal({
      title: '确认清空',
      content: '确定要清空所有收藏吗？',
      success: (res) => {
        if (res.confirm) {
          wx.setStorageSync('favorites', []);
          wx.setStorageSync('favoriteCount', 0);
          
          this.setData({
            favorites: [],
            isEmpty: true
          });
          
          wx.showToast({
            title: '已清空',
            icon: 'success'
          });
        }
      }
    });
  },

  // 去查找院校
  goToSearch() {
    wx.switchTab({
      url: '/pages/university/university'
    });
  }
});
