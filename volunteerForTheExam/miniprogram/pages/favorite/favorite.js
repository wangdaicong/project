// pages/favorite/favorite.js
Page({
  data: {
    favorites: [],
    loading: false,
    userId: 1 // 实际应从登录状态获取
  },

  onLoad() {
    this.loadFavorites();
  },

  onShow() {
    this.loadFavorites();
  },

  // 加载收藏列表
  loadFavorites() {
    this.setData({ loading: true });
    
    wx.request({
      url: 'http://localhost:8080/api/favorite/list',
      method: 'GET',
      data: { userId: this.data.userId },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            favorites: res.data.data,
            loading: false
          });
        }
      },
      fail: () => {
        this.setData({ loading: false });
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    });
  },

  // 取消收藏
  removeFavorite(e) {
    const universityId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确定要取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: 'http://localhost:8080/api/favorite/remove',
            method: 'POST',
            data: {
              userId: this.data.userId,
              universityId: universityId
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '取消成功',
                  icon: 'success'
                });
                this.loadFavorites();
              }
            }
          });
        }
      }
    });
  },

  // 查看详情
  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/university-detail/university-detail?id=${id}`
    });
  },

  // 院校对比
  compareUniversities() {
    const favorites = this.data.favorites;
    
    if (favorites.length < 2) {
      wx.showToast({
        title: '至少选择2所院校',
        icon: 'none'
      });
      return;
    }
    
    if (favorites.length > 4) {
      wx.showToast({
        title: '最多选择4所院校',
        icon: 'none'
      });
      return;
    }
    
    const ids = favorites.map(item => item.id).join(',');
    wx.navigateTo({
      url: `/pages/compare/compare?ids=${ids}`
    });
  }
});
