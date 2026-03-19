const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    id: null,
    university: {},
    majors: []
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadUniversityDetail();
      this.loadMajors();
    }
  },

  loadUniversityDetail() {
    wx.showLoading({ title: '加载中...' });
    
    request.get(`/university/${this.data.id}`).then(res => {
      console.log('院校详情数据:', res);
      
      // 确保数据完整性
      const university = {
        ...res,
        introduction: res.introduction || '暂无简介',
        features: res.features || '暂无特色介绍',
        address: res.address || '暂无地址信息',
        phone: res.phone || '暂无联系电话',
        website: res.website || '暂无官网信息',
        minScore: res.minScore || null,
        maxScore: res.maxScore || null,
        ranking: res.ranking || null
      };
      
      this.setData({ university: university });
      wx.setNavigationBarTitle({
        title: res.name || '院校详情'
      });
      wx.hideLoading();
    }).catch(err => {
      console.error('加载学校详情失败', err);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  loadMajors() {
    request.get(`/major/university/${this.data.id}`).then(res => {
      this.setData({ majors: res || [] });
    }).catch(err => {
      console.error('加载专业列表失败', err);
    });
  },

  addToCompare() {
    app.addToCompare(this.data.university);
  },

  callPhone() {
    const phone = this.data.university.phone;
    if (!phone) {
      wx.showToast({
        title: '暂无联系电话',
        icon: 'none'
      });
      return;
    }
    wx.makePhoneCall({
      phoneNumber: phone
    });
  },

  openWebsite() {
    const website = this.data.university.website;
    if (!website) {
      wx.showToast({
        title: '暂无官网信息',
        icon: 'none'
      });
      return;
    }
    wx.setClipboardData({
      data: website,
      success() {
        wx.showToast({
          title: '网址已复制',
          icon: 'success'
        });
      }
    });
  },

  goToMajorDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/major/detail?id=${id}`
    });
  }
})
