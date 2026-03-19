const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    keyword: '',
    showFilter: false,
    filterProvince: [],
    levelIndex: 0,
    typeIndex: 0,
    levels: ['全部', '985', '211', '双一流', '普通本科'],
    types: ['全部', '综合', '理工', '师范', '医药', '财经', '政法', '农林', '艺术'],
    minScore: '',
    maxScore: '',
    universities: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    isRecommend: false,
    userScore: null
  },

  onLoad(options) {
    if (options.recommend === 'true') {
      this.setData({
        isRecommend: true,
        userScore: app.globalData.userScore
      });
      this.loadRecommendations();
    } else if (options.category) {
      this.searchByCategory(options.category);
    } else {
      this.loadUniversities();
    }
  },

  onShow() {
    // 检查是否是推荐模式
    if (app.globalData.isRecommendMode) {
      app.globalData.isRecommendMode = false; // 重置标识
      this.setData({
        isRecommend: true,
        userScore: app.globalData.userScore
      });
      this.loadRecommendations();
    }
  },

  onSearchInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onSearch() {
    this.setData({
      pageNum: 1,
      universities: []
    });
    this.loadUniversities();
  },

  toggleFilter() {
    this.setData({
      showFilter: !this.data.showFilter
    });
  },

  onProvinceFilterChange(e) {
    this.setData({
      filterProvince: e.detail.value
    });
  },

  onLevelChange(e) {
    this.setData({
      levelIndex: e.detail.value
    });
  },

  onTypeChange(e) {
    this.setData({
      typeIndex: e.detail.value
    });
  },

  onMinScoreInput(e) {
    this.setData({
      minScore: e.detail.value
    });
  },

  onMaxScoreInput(e) {
    this.setData({
      maxScore: e.detail.value
    });
  },

  resetFilter() {
    this.setData({
      filterProvince: [],
      levelIndex: 0,
      typeIndex: 0,
      minScore: '',
      maxScore: ''
    });
  },

  applyFilter() {
    this.setData({
      pageNum: 1,
      universities: [],
      showFilter: false
    });
    this.loadUniversities();
  },

  loadUniversities() {
    if (this.data.loading) return;

    this.setData({ loading: true });

    const params = {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize
    };

    if (this.data.filterProvince.length > 0) {
      params.province = this.data.filterProvince[0];
    }
    if (this.data.levelIndex > 0) {
      params.level = this.data.levels[this.data.levelIndex];
    }
    if (this.data.typeIndex > 0) {
      params.type = this.data.types[this.data.typeIndex];
    }
    if (this.data.minScore) {
      params.minScore = parseInt(this.data.minScore);
    }
    if (this.data.maxScore) {
      params.maxScore = parseInt(this.data.maxScore);
    }

    request.get('/university/list', params).then(res => {
      const newList = this.data.universities.concat(res.records || []);
      this.setData({
        universities: newList,
        hasMore: res.current < res.pages,
        loading: false
      });
    }).catch(err => {
      console.error('加载院校失败', err);
      this.setData({ loading: false });
    });
  },

  loadRecommendations() {
    if (!app.globalData.userScore) {
      wx.showToast({
        title: '请先输入分数',
        icon: 'none'
      });
      return;
    }

    const score = app.globalData.userScore;
    const province = app.globalData.userProvince;

    // 设置模拟推荐数据
    this.setData({
      recommendations: [
        { id: 1, name: '清华大学', matchScore: 95 },
        { id: 2, name: '北京大学', matchScore: 92 }
      ]
    });

    request.get('/university/recommend', {
      score: score,
      province: province
    }).then(res => {
      if (res && res.length > 0) {
        this.setData({
          recommendations: res
        });
      }
    }).catch(err => {
      console.error('加载推荐院校失败，使用模拟数据', err);
    });
  },

  searchByCategory(category) {
    this.setData({
      keyword: category,
      pageNum: 1,
      universities: []
    });
    this.loadUniversities();
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({
      pageNum: this.data.pageNum + 1
    });
    this.loadUniversities();
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/university/detail?id=${id}`
    });
  },

  addToCompare(e) {
    const item = e.currentTarget.dataset.item;
    app.addToCompare(item);
  }
})
