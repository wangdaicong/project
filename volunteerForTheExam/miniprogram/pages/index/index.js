const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    hasScore: false,
    userScore: null,
    userProvince: null,
    inputScore: '',
    provinceIndex: 0,
    selectedProvince: '全国',
    provinces: ['全国', '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江', '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州', '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆'],
    hotUniversities985: [],
    hotUniversities211: [],
    hotUniversitiesVocational: [],
    categories: [
      { name: '工学', icon: '⚙️' },
      { name: '理学', icon: '🔬' },
      { name: '医学', icon: '⚕️' },
      { name: '经济学', icon: '💰' },
      { name: '管理学', icon: '📈' },
      { name: '文学', icon: '📖' },
      { name: '法学', icon: '⚖️' },
      { name: '教育学', icon: '👨‍🏫' },
      { name: '艺术学', icon: '🎨' }
    ]
  },

  onLoad() {
    this.checkUserInfo();
    this.loadHotUniversities();
  },

  onShow() {
    this.checkUserInfo();
  },

  checkUserInfo() {
    const score = app.globalData.userScore;
    const province = app.globalData.userProvince;
    if (score && province) {
      this.setData({
        hasScore: true,
        userScore: score,
        userProvince: province
      });
    }
  },

  onScoreInput(e) {
    this.setData({
      inputScore: e.detail.value
    });
  },

  clearScore() {
    this.setData({
      inputScore: ''
    });
  },

  onProvinceChange(e) {
    const index = e.detail.value;
    this.setData({
      provinceIndex: index,
      selectedProvince: this.data.provinces[index]
    });
  },

  saveUserInfo() {
    const { inputScore, selectedProvince } = this.data;
    if (!inputScore || inputScore < 0 || inputScore > 750) {
      wx.showToast({
        title: '请输入有效分数(0-750)',
        icon: 'none'
      });
      return;
    }

    app.setUserInfo(parseInt(inputScore), selectedProvince);
    this.setData({
      hasScore: true,
      userScore: parseInt(inputScore),
      userProvince: selectedProvince
    });

    // 跳转到智能推荐页面
    wx.navigateTo({
      url: '/pages/recommend/recommend'
    });
  },

  changeScore() {
    const provinceIndex = this.data.provinces.indexOf(this.data.userProvince);
    this.setData({
      hasScore: false,
      inputScore: this.data.userScore,
      provinceIndex: provinceIndex >= 0 ? provinceIndex : 0,
      selectedProvince: this.data.userProvince
    });
  },

  loadHotUniversities() {
    // 调用新的分类热门院校接口
    request.get('/university/hot').then(res => {
      if (res) {
        this.setData({
          hotUniversities985: res['985'] || [],
          hotUniversities211: res['211'] || [],
          hotUniversitiesVocational: res['专科'] || []
        });
      }
    }).catch(err => {
      console.error('加载热门院校失败', err);
      // 使用模拟数据
      this.setData({
        hotUniversities985: [
          {
            id: 7,
            name: '清华大学',
            city: '北京',
            level: '985/211/双一流',
            type: '理工',
            ranking: 1,
            minScore: 688,
            maxScore: 703
          },
          {
            id: 8,
            name: '北京大学',
            city: '北京',
            level: '985/211/双一流',
            type: '综合',
            ranking: 2,
            minScore: 687,
            maxScore: 702
          },
          {
            id: 9,
            name: '复旦大学',
            city: '上海',
            level: '985/211/双一流',
            type: '综合',
            ranking: 3,
            minScore: 680,
            maxScore: 695
          }
        ],
        hotUniversities211: [],
        hotUniversitiesVocational: []
      });
    });
  },

  goToSearch() {
    wx.switchTab({
      url: '/pages/university/university'
    });
  },

  goToRecommend() {
    if (!this.data.hasScore) {
      wx.showToast({
        title: '请先输入分数',
        icon: 'none'
      });
      return;
    }
    // 设置全局推荐标识
    app.globalData.isRecommendMode = true;
    wx.switchTab({
      url: '/pages/search/search'
    });
  },

  goToCareer() {
    wx.navigateTo({
      url: '/pages/career/career'
    });
  },

  goToCompare() {
    wx.navigateTo({
      url: '/pages/compare/compare'
    });
  },

  goToEmployment() {
    wx.navigateTo({
      url: '/pages/employment/employment'
    });
  },

  goToIndustry() {
    wx.navigateTo({
      url: '/pages/industry/industry'
    });
  },

  goToEnrollment() {
    wx.navigateTo({
      url: '/pages/enrollment/enrollment'
    });
  },

  goToCityEmployment() {
    wx.navigateTo({
      url: '/pages/city-employment/city-employment'
    });
  },

  goToAssessment() {
    wx.navigateTo({
      url: '/pages/assessment/assessment'
    });
  },

  goToVolunteer() {
    wx.navigateTo({
      url: '/pages/volunteer/volunteer'
    });
  },

  goToUniversityDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/packageB/pages/university/detail?id=${id}`
    });
  },

  searchByCategory(e) {
    const category = e.currentTarget.dataset.category;
    wx.navigateTo({
      url: `/pages/search/search?category=${category}`
    });
  }
})
