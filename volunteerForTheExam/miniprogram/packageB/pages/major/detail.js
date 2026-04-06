// packageB/pages/major/detail.js
const request = require('../../../utils/request');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    id: null,
    major: {},
    currentTab: 0,
    guide: {}
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadMajorDetail();
      this.loadMajorGuide();
    }
  },

  // 加载专业详情
  loadMajorDetail() {
    wx.showLoading({ title: '加载中...' });
    
    request.get(`/major/${this.data.id}`).then(res => {
      console.log('专业详情数据:', res);
      console.log('[DEBUG] tags字段:', res?.tags);
      // 兼容处理tags字段，并拆分为数组
      const tags = res.tags || '';
      const tagsArray = tags ? tags.split(',').map(t => t.trim()).filter(t => t) : [];
      const major = {
        ...res,
        tags: tags,
        tagsArray: tagsArray
      };
      this.setData({ major });
      wx.setNavigationBarTitle({
        title: res.name || '专业详情'
      });
      wx.hideLoading();
    }).catch(err => {
      console.error('加载专业详情失败', err);
      wx.hideLoading();
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  // 加载避坑指南
  loadMajorGuide() {
    console.log('[DEBUG] 开始加载专业指南，ID:', this.data.id);
    
    request.get(`/major/guide/${this.data.id}`).then(res => {
      console.log('[DEBUG] 专业指南API返回:', res);
      console.log('[DEBUG] tags:', res?.tags);
      console.log('[DEBUG] employment:', res?.employment);
      console.log('[DEBUG] civilService:', res?.civilService);
      
      this.setData({ guide: res || {} }, () => {
        console.log('[DEBUG] setData完成，当前guide:', this.data.guide);
      });
    }).catch(err => {
      console.error('[DEBUG] 加载专业指南失败:', err);
      this.setData({ guide: {} });
    });
  },

  // 切换Tab
  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab: index });
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})