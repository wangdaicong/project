const request = require('../../utils/request');

Page({
  data: {
    id: null,
    major: {},
    relatedCareers: []
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadMajorDetail();
    }
  },

  loadMajorDetail() {
    request.get(`/major/${this.data.id}`).then(res => {
      this.setData({ major: res });
      wx.setNavigationBarTitle({
        title: res.name
      });
      
      if (res.category) {
        this.loadRelatedCareers(res.category);
      }
    }).catch(err => {
      console.error('加载专业详情失败', err);
    });
  },

  loadRelatedCareers(category) {
    request.get('/career/search', {
      keyword: category
    }).then(res => {
      this.setData({ 
        relatedCareers: (res || []).slice(0, 5)
      });
    }).catch(err => {
      console.error('加载相关职业失败', err);
    });
  },

  goToCareerDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/career/detail?id=${id}`
    });
  }
})
