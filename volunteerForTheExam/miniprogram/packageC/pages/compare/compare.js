const app = getApp();

Page({
  data: {
    compareList: [],
    showResult: false,
    compareData: null
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
      compareList: app.globalData.compareList || [],
      showResult: false
    });
  },

  startCompare() {
    const compareList = this.data.compareList;
    if (compareList.length < 2) {
      wx.showToast({
        title: '至少选择2所院校',
        icon: 'none'
      });
      return;
    }

    // 生成对比数据
    const compareData = this.generateCompareData(compareList);
    this.setData({
      showResult: true,
      compareData
    });

    // 滚动到对比结果
    wx.pageScrollTo({
      selector: '.compare-result',
      duration: 300
    });
  },

  generateCompareData(list) {
    return {
      basic: [
        { label: '院校名称', values: list.map(item => item.name) },
        { label: '所在地区', values: list.map(item => `${item.province} ${item.city}`) },
        { label: '办学层次', values: list.map(item => item.level || '-') },
        { label: '学校类型', values: list.map(item => item.type || '-') }
      ],
      score: [
        { label: '全国排名', values: list.map(item => item.ranking || '-') },
        { label: '最低分数', values: list.map(item => item.minScore || '-') },
        { label: '最高分数', values: list.map(item => item.maxScore || '-') },
        { label: '平均分数', values: list.map(item => {
          if (item.minScore && item.maxScore) {
            return Math.round((item.minScore + item.maxScore) / 2);
          }
          return '-';
        }) }
      ]
    };
  },

  clearAll() {
    wx.showModal({
      title: '确认清空',
      content: '确定要清空所有对比院校吗？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.compareList = [];
          this.setData({
            compareList: [],
            showResult: false
          });
          wx.showToast({
            title: '已清空',
            icon: 'success'
          });
        }
      }
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/packageB/pages/university/detail?id=${id}`
    });
  },

  goToSearch() {
    wx.switchTab({
      url: '/pages/search/search'
    });
  }
})
