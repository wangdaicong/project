const request = require('../../utils/request');

Page({
  data: {
    id: null,
    career: {},
    relatedMajors: []
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadCareerDetail();
    }
  },

  loadCareerDetail() {
    request.get(`/career/${this.data.id}`).then(res => {
      const majors = res.relatedMajors ? res.relatedMajors.split(',') : [];
      this.setData({ 
        career: res,
        relatedMajors: majors
      });
      wx.setNavigationBarTitle({
        title: res.position
      });
    }).catch(err => {
      console.error('加载职业详情失败', err);
    });
  }
})
