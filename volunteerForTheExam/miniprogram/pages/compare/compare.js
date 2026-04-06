// pages/compare/compare.js
Page({
  data: {
    universities: [],
    scoreLines: {},
    loading: false
  },

  onLoad(options) {
    const ids = options.ids.split(',');
    this.loadCompareData(ids);
  },

  // 加载对比数据
  loadCompareData(ids) {
    this.setData({ loading: true });
    
    wx.request({
      url: 'http://localhost:8080/api/favorite/compare',
      method: 'POST',
      data: { universityIds: ids },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data;
          
          // 组织分数线数据
          const scoreLines = {};
          data.scoreLines.forEach(item => {
            if (!scoreLines[item.university_id]) {
              scoreLines[item.university_id] = [];
            }
            scoreLines[item.university_id].push(item);
          });
          
          this.setData({
            universities: data.universities,
            scoreLines: scoreLines,
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

  // 查看趋势图
  viewTrend(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/score-trend/score-trend?universityId=${id}`
    });
  }
});
