const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    id: null,
    university: {},
    majors: [],
    isFavorite: false,
    isInCompare: false,
    logoText: '',
    logoGradient: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadUniversityDetail();
      this.loadMajors();
      this.checkFavoriteStatus();
      this.checkCompareStatus();
    }
  },

  onShow() {
    this.checkCompareStatus();
  },

  // 检查收藏状态
  checkFavoriteStatus() {
    const favorites = wx.getStorageSync('favorites') || [];
    const isFavorite = favorites.some(item => item.id === parseInt(this.data.id));
    this.setData({ isFavorite });
  },

  // 检查对比状态
  checkCompareStatus() {
    const compareList = app.globalData.compareList || [];
    const isInCompare = compareList.some(item => item.id === parseInt(this.data.id));
    this.setData({ isInCompare });
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
      
      // 生成logo文字和渐变色
      const logoData = this.generateLogoData(res.name);
      
      this.setData({ 
        university: university,
        logoText: logoData.text,
        logoGradient: logoData.gradient
      });
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

  // 切换收藏状态
  toggleFavorite() {
    const favorites = wx.getStorageSync('favorites') || [];
    const universityId = parseInt(this.data.id);
    const index = favorites.findIndex(item => item.id === universityId);
    
    if (index > -1) {
      // 已收藏，取消收藏
      favorites.splice(index, 1);
      wx.showToast({
        title: '已取消收藏',
        icon: 'success'
      });
      this.setData({ isFavorite: false });
    } else {
      // 未收藏，添加收藏
      favorites.push({
        id: universityId,
        name: this.data.university.name,
        province: this.data.university.province,
        city: this.data.university.city,
        level: this.data.university.level,
        type: this.data.university.type,
        ranking: this.data.university.ranking,
        minScore: this.data.university.minScore,
        maxScore: this.data.university.maxScore,
        logoUrl: this.data.university.logoUrl,
        favoriteTime: new Date().getTime()
      });
      wx.showToast({
        title: '收藏成功',
        icon: 'success'
      });
      this.setData({ isFavorite: true });
    }
    
    wx.setStorageSync('favorites', favorites);
    wx.setStorageSync('favoriteCount', favorites.length);
  },

  // 添加到对比
  addToCompare() {
    const success = app.addToCompare(this.data.university);
    if (success) {
      this.setData({ isInCompare: true });
    }
  },

  // 从对比中移除
  removeFromCompare() {
    app.removeFromCompare(parseInt(this.data.id));
    this.setData({ isInCompare: false });
    wx.showToast({
      title: '已移除对比',
      icon: 'success'
    });
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
  },

  // 查看分数线趋势
  viewScoreTrend() {
    wx.navigateTo({
      url: `/pages/score-trend/score-trend?id=${this.data.id}&name=${this.data.university.name}`
    });
  },

  // 生成logo数据（首字母+渐变色）
  generateLogoData(name) {
    if (!name) {
      return {
        text: '?',
        gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      };
    }

    // 提取院校名称的前两个字作为logo文字
    const text = name.length >= 2 ? name.substring(0, 2) : name;

    // 根据院校名称生成唯一的渐变色
    const gradients = [
      'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 紫色
      'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', // 粉红
      'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', // 蓝色
      'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', // 绿色
      'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', // 橙粉
      'linear-gradient(135deg, #30cfd0 0%, #330867 100%)', // 青紫
      'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)', // 淡蓝粉
      'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)', // 淡粉
      'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', // 橙色
      'linear-gradient(135deg, #ff6e7f 0%, #bfe9ff 100%)', // 红蓝
      'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)', // 紫蓝
      'linear-gradient(135deg, #f8b500 0%, #fceabb 100%)'  // 金黄
    ];

    // 使用院校名称的字符码生成索引
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % gradients.length;

    return {
      text: text,
      gradient: gradients[index]
    };
  }
});
