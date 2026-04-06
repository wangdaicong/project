const request = require('../../utils/request');

Page({
  data: {
    currentTab: 'career',
    keyword: '',
    majorKeyword: '',
    hotCareers: [],
    industries: [
      { name: '互联网', icon: '💻', count: 156 },
      { name: '金融', icon: '💰', count: 89 },
      { name: '教育', icon: '👨‍🏫', count: 67 },
      { name: '医疗', icon: '⚕️', count: 78 },
      { name: '制造业', icon: '⚙️', count: 92 },
      { name: '服务业', icon: '🛎️', count: 54 }
    ],
    careers: [],
    majorData: null,
    employmentData: {},
    industryDistribution: [],
    majorCareers: [],
    hotMajors: [],
    loading: false
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      currentTab: tab
    });
    if (tab === 'major' && this.data.hotMajors.length === 0) {
      this.loadHotMajors();
    }
  },

  onLoad() {
    this.loadHotCareers();
    this.loadCareers();
  },

  onMajorSearchInput(e) {
    this.setData({
      majorKeyword: e.detail.value
    });
  },

  onMajorSearch() {
    const keyword = this.data.majorKeyword.trim();
    if (!keyword) {
      wx.showToast({
        title: '请输入专业名称',
        icon: 'none'
      });
      return;
    }
    this.searchMajorEmployment(keyword);
  },

  searchMajorEmployment(keyword) {
    wx.showLoading({ title: '搜索中...' });
    
    request.get('/major/search', { keyword }).then(res => {
      if (res && res.length > 0) {
        const major = res[0];
        this.loadMajorEmploymentData(major.id);
      } else {
        wx.showToast({
          title: '未找到该专业',
          icon: 'none'
        });
      }
    }).catch(err => {
      console.error('搜索专业失败', err);
      wx.showToast({
        title: '搜索失败',
        icon: 'none'
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },

  loadMajorEmploymentData(majorId) {
    wx.showLoading({ title: '加载中...' });
    
    request.get(`/employment/major/${majorId}`).then(res => {
      const distribution = this.processIndustryDistribution(res.industryDistribution);
      this.setData({
        majorData: { id: majorId, name: res.name, category: res.category },
        employmentData: res,
        industryDistribution: distribution
      });
      this.loadMajorCareers(majorId);
    }).catch(err => {
      console.error('加载专业就业数据失败', err);
      // 使用模拟数据
      this.setData({
        majorData: { id: majorId, name: this.data.majorKeyword, category: '工学' },
        employmentData: {
          employmentRate: 95.8,
          avgSalary: 12000,
          matchRate: 85,
          upgradeRate: 25,
          typicalJobs: ['软件工程师', '系统架构师', '技术经理', '产品经理']
        },
        industryDistribution: [
          { name: '互联网', percentage: 45 },
          { name: '金融', percentage: 20 },
          { name: '制造业', percentage: 15 },
          { name: '其他', percentage: 20 }
        ]
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },

  processIndustryDistribution(distribution) {
    if (!distribution) return [];
    const total = Object.values(distribution).reduce((sum, val) => sum + val, 0);
    return Object.entries(distribution).map(([name, value]) => ({
      name: name,
      value: value,
      percentage: total > 0 ? Math.round((value / total) * 100) : 0
    }));
  },

  loadMajorCareers(majorId) {
    request.get(`/employment/careers/by-major/${majorId}`).then(res => {
      this.setData({
        majorCareers: res || []
      });
    }).catch(err => {
      console.error('加载对口职业失败', err);
      this.setData({
        majorCareers: [
          {
            id: 1,
            name: '软件工程师',
            matchDegree: 95,
            salaryRange: '15K-40K',
            employmentPercentage: 35,
            skillRequirements: ['Java', 'Python', '数据库']
          },
          {
            id: 2,
            name: '系统架构师',
            matchDegree: 88,
            salaryRange: '25K-60K',
            employmentPercentage: 20,
            skillRequirements: ['架构设计', '分布式系统', '微服务']
          }
        ]
      });
    });
  },

  loadHotMajors() {
    request.get('/major/hot-employment').then(res => {
      this.setData({
        hotMajors: res || []
      });
    }).catch(err => {
      console.error('加载热门专业失败', err);
      this.setData({
        hotMajors: [
          { id: 1, name: '计算机科学与技术', avgSalary: 12000, employmentRate: 95.8 },
          { id: 2, name: '软件工程', avgSalary: 13000, employmentRate: 96.2 },
          { id: 3, name: '人工智能', avgSalary: 15000, employmentRate: 97.5 }
        ]
      });
    });
  },

  onMajorTap(e) {
    const id = e.currentTarget.dataset.id;
    this.loadMajorEmploymentData(id);
  },

  onSearchInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onSearch() {
    if (!this.data.keyword) {
      this.loadCareers();
      return;
    }
    
    this.setData({ loading: true });
    request.get('/career/search', {
      keyword: this.data.keyword
    }).then(res => {
      this.setData({
        careers: res || [],
        loading: false
      });
    }).catch(err => {
      console.error('搜索职业失败', err);
      this.setData({ loading: false });
    });
  },

  loadHotCareers() {
    // 设置模拟数据
    this.setData({
      hotCareers: [
        { id: 1, position: '软件工程师', industry: '互联网', salary: '15K-40K', demandIndex: 95 },
        { id: 2, position: '金融分析师', industry: '金融', salary: '12K-35K', demandIndex: 85 }
      ]
    });

    request.get('/career/hot').then(res => {
      if (res && res.length > 0) {
        this.setData({
          hotCareers: res
        });
      }
    }).catch(err => {
      console.error('加载热门职业失败，使用模拟数据', err);
    });
  },

  loadCareers() {
    const { selectedIndustry, searchKeyword } = this.data;
    const params = {};
    
    if (selectedIndustry !== '全部') {
      params.industry = selectedIndustry;
    }
    if (searchKeyword) {
      params.keyword = searchKeyword;
    }

    // 设置模拟数据
    const mockCareers = [
      { id: 1, position: '软件工程师', industry: '互联网', salary: '15K-40K', demandIndex: 95 },
      { id: 2, position: '金融分析师', industry: '金融', salary: '12K-35K', demandIndex: 85 },
      { id: 3, position: '中学教师', industry: '教育', salary: '8K-18K', demandIndex: 75 },
      { id: 4, position: '临床医生', industry: '医疗', salary: '10K-30K', demandIndex: 88 }
    ];
    
    this.setData({
      careers: mockCareers
    });

    request.get('/career/industry', params).then(res => {
      if (res && res.length > 0) {
        this.setData({
          careers: res
        });
      }
    }).catch(err => {
      console.error('加载职业列表失败，使用模拟数据', err);
    });
  },

  filterByIndustry(e) {
    const industry = e.currentTarget.dataset.industry;
    this.setData({
      selectedIndustry: industry
    });
    
    this.loadCareers();
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/career/detail?id=${id}`
    });
  }
})
