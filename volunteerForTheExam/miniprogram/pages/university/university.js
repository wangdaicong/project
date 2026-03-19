const request = require('../../utils/request');

Page({
  data: {
    keyword: '',
    showFilter: false,
    levelIndex: 0,
    typeIndex: 0,
    provinceIndex: 0,
    levels: ['全部', '985', '211', '双一流', '普通本科', '专科'],
    types: ['全部', '综合', '理工', '师范', '医药', '财经', '政法', '农林', '艺术'],
    provinces: ['全部', '北京', '上海', '天津', '重庆', '江苏', '浙江', '广东', '山东', '河南', '湖北', '湖南', '四川', '陕西'],
    universities: [],
    loading: false,
    networkError: false,
    pageNum: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad() {
    this.loadUniversities();
  },

  onShow() {
    this.loadUniversities();
  },

  loadRecommendations(score) {
    wx.showLoading({ title: '智能推荐中...' });
    
    const app = getApp();
    const province = app.globalData.userProvince || '全国';
    
    request.get(`/university/recommend?score=${score}&province=${province}`).then(res => {
      this.setData({
        universities: res || [],
        currentTab: '推荐'
      });
      
      wx.showToast({
        title: `为您推荐${res.length}所院校`,
        icon: 'success'
      });
    }).catch(err => {
      console.error('加载推荐失败', err);
      // 推荐失败，使用模拟数据
      this.setData({
        universities: this.getMockRecommendations(score),
        currentTab: '推荐'
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },

  getMockRecommendations(score) {
    // 根据分数返回模拟推荐数据
    if (score >= 650) {
      return [
        {
          id: 1,
          name: '清华大学',
          level: '985/211/双一流',
          province: '北京',
          city: '北京',
          type: '理工',
          ranking: 1,
          minScore: 688,
          probability: '冲刺',
          probabilityClass: 'probability-rush'
        },
        {
          id: 2,
          name: '北京大学',
          level: '985/211/双一流',
          province: '北京',
          city: '北京',
          type: '综合',
          ranking: 2,
          minScore: 685,
          probability: '冲刺',
          probabilityClass: 'probability-rush'
        }
      ];
    } else if (score >= 600) {
      return [
        {
          id: 3,
          name: '浙江大学',
          level: '985/211/双一流',
          province: '浙江',
          city: '杭州',
          type: '综合',
          ranking: 4,
          minScore: 660,
          probability: '稳妥',
          probabilityClass: 'probability-stable'
        },
        {
          id: 4,
          name: '上海交通大学',
          level: '985/211/双一流',
          province: '上海',
          city: '上海',
          type: '理工',
          ranking: 3,
          minScore: 670,
          probability: '冲刺',
          probabilityClass: 'probability-rush'
        }
      ];
    } else {
      return [
        {
          id: 5,
          name: '南京大学',
          level: '985/211/双一流',
          province: '江苏',
          city: '南京',
          type: '综合',
          ranking: 6,
          minScore: 640,
          probability: '稳妥',
          probabilityClass: 'probability-stable'
        }
      ];
    }
  },

  onSearchInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword) {
      wx.showToast({
        title: '请输入院校名称',
        icon: 'none'
      });
      return;
    }
    this.loadUniversities('', keyword);
  },

  toggleFilter() {
    this.setData({
      showFilter: !this.data.showFilter
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

  onProvinceChange(e) {
    this.setData({
      provinceIndex: e.detail.value
    });
  },

  // 应用筛选
  onApplyFilter() {
    this.setData({
      pageNum: 1,
      universities: []
    });
    this.loadUniversities();
  },

  // 重置筛选
  onResetFilter() {
    this.setData({
      levelIndex: 0,
      typeIndex: 0,
      provinceIndex: 0,
      keyword: '',
      pageNum: 1,
      universities: []
    });
    this.loadUniversities();
  },

  loadUniversities(level = '', keyword = '') {
    this.setData({ loading: true, networkError: false });
    wx.showLoading({ title: '加载中...' });
    
    const { pageNum, pageSize, levelIndex, typeIndex, provinceIndex, levels, types, provinces } = this.data;
    
    // 构建请求参数
    let url = '/university/list';
    const params = [];
    params.push(`pageNum=${pageNum}`);
    params.push(`pageSize=${pageSize}`);
    
    // 层次筛选
    if (level) {
      params.push(`level=${encodeURIComponent(level)}`);
    } else if (levelIndex > 0) {
      params.push(`level=${encodeURIComponent(levels[levelIndex])}`);
    }
    
    // 类型筛选
    if (typeIndex > 0) {
      params.push(`type=${encodeURIComponent(types[typeIndex])}`);
    }
    
    // 省份筛选
    if (provinceIndex > 0) {
      params.push(`province=${encodeURIComponent(provinces[provinceIndex])}`);
    }
    
    // 关键词搜索
    if (keyword) {
      params.push(`keyword=${encodeURIComponent(keyword)}`);
    }
    
    if (params.length > 0) url += '?' + params.join('&');

    console.log('请求URL:', url);

    request.get(url).then(res => {
      console.log('后端返回数据:', res);
      
      // 处理分页数据
      const universities = res.records || res.data || res || [];
      const total = res.total || universities.length;
      const hasMore = pageNum * pageSize < total;
      
      // 确保每个院校都有完整的数据结构
      const processedUniversities = universities.map(uni => ({
        ...uni,
        introduction: uni.introduction || '',
        features: uni.features || '',
        address: uni.address || '',
        phone: uni.phone || '',
        website: uni.website || '',
        minScore: uni.minScore || null,
        maxScore: uni.maxScore || null,
        ranking: uni.ranking || null
      }));
      
      this.setData({
        universities: processedUniversities,
        hasMore: hasMore,
        loading: false,
        networkError: false
      });
      
      wx.hideLoading();
      
      if (universities.length === 0) {
        wx.showToast({
          title: '暂无数据',
          icon: 'none'
        });
      }
    }).catch(err => {
      console.error('加载院校失败', err);
      
      this.setData({
        universities: [],
        loading: false,
        networkError: true
      });
      
      wx.hideLoading();
      wx.showModal({
        title: '提示',
        content: '后端服务未启动或数据库未连接\n\n请执行以下步骤：\n1. 启动MySQL数据库\n2. 运行 start.bat 启动后端\n3. 确认数据已导入',
        showCancel: false
      });
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/university/detail?id=${id}`
    });
  },

  goToEnrollment() {
    wx.navigateTo({
      url: '/pages/enrollment/enrollment'
    });
  }
});
