const request = require('../../utils/request');

Page({
  data: {
    keyword: '',
    selectedCategory: '',
    categories: ['工学', '理学', '医学', '经济学', '管理学', '文学', '法学', '教育学', '艺术学'],
    majors: []
  },

  onLoad() {
    this.loadMajors();
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
        title: '请输入专业名称',
        icon: 'none'
      });
      return;
    }
    this.loadMajors(keyword);
  },

  onCategoryTap(e) {
    const category = e.currentTarget.dataset.category;
    this.setData({
      selectedCategory: category
    });
    this.loadMajors('', category);
  },

  loadMajors(keyword = '', category = '') {
    wx.showLoading({ title: '加载中...' });
    
    let url = '/major/list';
    const params = [];
    if (keyword) params.push(`keyword=${keyword}`);
    if (category) params.push(`category=${category}`);
    if (params.length > 0) url += '?' + params.join('&');

    request.get(url).then(res => {
      this.setData({
        majors: res || []
      });
    }).catch(err => {
      console.error('加载专业失败', err);
      this.setData({
        majors: [
          {
            id: 1,
            name: '计算机科学与技术',
            category: '工学',
            degree: '本科',
            employmentRate: 95.8,
            avgSalary: 12000,
            tags: ['高薪', '热门', '好就业']
          },
          {
            id: 2,
            name: '软件工程',
            category: '工学',
            degree: '本科',
            employmentRate: 96.2,
            avgSalary: 13000,
            tags: ['高薪', '互联网', '技术']
          },
          {
            id: 3,
            name: '人工智能',
            category: '工学',
            degree: '本科',
            employmentRate: 97.5,
            avgSalary: 15000,
            tags: ['前沿', '高薪', '热门']
          }
        ]
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/major/detail?id=${id}`
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

  goToCityEmployment() {
    wx.navigateTo({
      url: '/pages/city-employment/city-employment'
    });
  }
});
