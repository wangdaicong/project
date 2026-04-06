const app = getApp();

Page({
  data: {
    userScore: '',
    paths: [],
    selectedPath: null
  },

  onLoad() {
    this.loadPaths();
  },

  loadPaths() {
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/enrollment/paths`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const paths = res.data.data.map(item => ({
            ...item,
            universitiesArray: this.parseJSON(item.universities, []),
            majorsArray: this.parseJSON(item.majors, []),
            timelineArray: this.parseJSON(item.timeline, [])
          }));
          this.setData({
            paths: paths
          });
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  parseJSON(str, defaultValue) {
    try {
      return JSON.parse(str);
    } catch (e) {
      return defaultValue;
    }
  },

  onScoreInput(e) {
    this.setData({
      userScore: e.detail.value
    });
  },

  getRecommendation() {
    const score = parseInt(this.data.userScore);
    
    if (!score || score < 0 || score > 750) {
      wx.showToast({
        title: '请输入有效的分数（0-750）',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '分析中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/enrollment/recommend`,
      method: 'POST',
      data: {
        score: score,
        interests: []
      },
      success: (res) => {
        if (res.data.success) {
          const recommendations = res.data.data.map(item => {
            const path = item.path;
            return {
              ...path,
              matchScore: item.matchScore,
              suitable: item.suitable,
              universitiesArray: this.parseJSON(path.universities, []),
              majorsArray: this.parseJSON(path.majors, []),
              timelineArray: this.parseJSON(path.timeline, [])
            };
          });
          
          this.setData({
            paths: recommendations
          });

          wx.showToast({
            title: '推荐成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '推荐失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  onPathTap(e) {
    const pathId = e.currentTarget.dataset.id;
    
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/enrollment/path/${pathId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const path = res.data.data;
          this.setData({
            selectedPath: {
              ...path,
              universitiesArray: this.parseJSON(path.universities, []),
              majorsArray: this.parseJSON(path.majors, []),
              timelineArray: this.parseJSON(path.timeline, [])
            }
          });
        }
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  closeDetail() {
    this.setData({
      selectedPath: null
    });
  },

  stopPropagation() {
  }
});
