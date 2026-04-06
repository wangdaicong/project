const request = require('../../utils/request');

Page({
  data: {
    keyword: '',
    showFilter: false,
    levelIndex: 0,
    typeIndex: 0,
    provinceIndex: 0,
    supervisorIndex: 0,
    natureIndex: 0,
    specialIndex: 0,
    // 弹窗显示状态
    showProvinceSheet: false,
    showSupervisorSheet: false,
    showLevelSheet: false,
    showNatureSheet: false,
    showSpecialSheet: false,
    // 筛选选项
    levels: ['全部', '本科', '专科'],
    types: ['全部', '综合', '理工', '师范', '医药', '财经', '政法', '农林', '艺术', '语言', '体育', '民族'],
    provinces: ['全部', '北京', '上海', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江', '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州', '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆'],
    supervisors: ['全部', '教育部', '其他部委', '地方', '军校'],
    natures: ['全部', '双一流建设高校', '民办高校', '独立学院', '中外合作办学', '内地与港澳台地区合作办学'],
    specialTags: ['全部', '985', '211', '双一流'],
    universities: [],
    loading: false,
    networkError: false,
    page: 1,
    size: 10,
    total: 0,
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

  // 显示省份弹窗
  showProvincePopup() {
    this.setData({ showProvinceSheet: true });
  },

  // 显示主管部门弹窗
  showSupervisorPopup() {
    this.setData({ showSupervisorSheet: true });
  },

  // 显示办学层次弹窗
  showLevelPopup() {
    this.setData({ showLevelSheet: true });
  },

  // 显示办学性质弹窗
  showNaturePopup() {
    this.setData({ showNatureSheet: true });
  },

  // 显示特殊标签弹窗
  showSpecialPopup() {
    this.setData({ showSpecialSheet: true });
  },

  // 切换筛选面板显示/隐藏
  toggleFilter() {
    this.setData({
      showFilter: !this.data.showFilter
    });
  },

  // 隐藏所有弹窗
  hideAllPopup() {
    this.setData({
      showProvinceSheet: false,
      showSupervisorSheet: false,
      showLevelSheet: false,
      showNatureSheet: false,
      showSpecialSheet: false
    });
  },

  // 确定筛选
  onConfirmFilter() {
    this.setData({
      showFilter: false,
      page: 1,
      universities: []
    });
    this.loadUniversities();
  },

  // 应用筛选
  onApplyFilter() {
    this.setData({
      page: 1,
      universities: [],
      showFilter: false
    });
    this.loadUniversities();
  },

  onLevelChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      levelIndex: index
    });
    this.hideAllPopup();
  },

  onTypeChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      typeIndex: index
    });
  },

  onProvinceChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      provinceIndex: index
    });
    this.hideAllPopup();
  },

  onSupervisorChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      supervisorIndex: index
    });
    this.hideAllPopup();
  },

  onNatureChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      natureIndex: index
    });
    this.hideAllPopup();
  },

  onSpecialChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      specialIndex: index
    });
    this.hideAllPopup();
  },

  // 重置筛选
  onResetFilter() {
    this.setData({
      levelIndex: 0,
      typeIndex: 0,
      provinceIndex: 0,
      supervisorIndex: 0,
      natureIndex: 0,
      specialIndex: 0,
      keyword: '',
      page: 1,
      universities: []
    });
    this.loadUniversities();
  },

  // 加载更多
  loadMore() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({
      page: this.data.page + 1
    });
    this.loadUniversities(true);
  },

  // 页面滚动到底部
  onReachBottom() {
    this.loadMore();
  },

  loadUniversities(append = false) {
    this.setData({ loading: true, networkError: false });
    if (!append) {
      wx.showLoading({ title: '加载中...' });
    }
    
    const { page, size, levelIndex, typeIndex, provinceIndex, supervisorIndex, natureIndex, specialIndex, 
            levels, types, provinces, supervisors, natures, specialTags, keyword } = this.data;
    
    // 构建请求参数
    let url = '/university/search';
    const params = [];
    params.push(`page=${page}`);
    params.push(`size=${size}`);
    
    // 关键词搜索
    if (keyword && keyword.trim()) {
      params.push(`keyword=${encodeURIComponent(keyword.trim())}`);
    }
    
    // 省份筛选
    if (provinceIndex > 0) {
      params.push(`province=${encodeURIComponent(provinces[provinceIndex])}`);
    }
    
    // 主管部门筛选
    if (supervisorIndex > 0) {
      params.push(`supervisor=${encodeURIComponent(supervisors[supervisorIndex])}`);
    }
    
    // 办学层次筛选
    if (levelIndex > 0) {
      params.push(`level=${encodeURIComponent(levels[levelIndex])}`);
    }
    
    // 办学性质筛选
    if (natureIndex > 0) {
      params.push(`schoolNature=${encodeURIComponent(natures[natureIndex])}`);
    }
    
    // 类型筛选
    if (typeIndex > 0) {
      params.push(`type=${encodeURIComponent(types[typeIndex])}`);
    }
    
    // 特殊标签筛选（985/211/双一流）
    if (specialIndex > 0) {
      const tag = specialTags[specialIndex];
      if (tag === '985') {
        params.push('is985=true');
      } else if (tag === '211') {
        params.push('is211=true');
      } else if (tag === '双一流') {
        params.push('isDoubleFirstClass=true');
      }
    }
    
    if (params.length > 0) url += '?' + params.join('&');

    console.log('请求URL:', url);

    request.get(url).then(res => {
      console.log('后端返回数据:', res);
      
      if (!res.success) {
        throw new Error(res.message || '加载失败');
      }
      
      // 处理分页数据
      const universities = res.data || [];
      const total = res.total || 0;
      const totalPages = res.totalPages || 1;
      const hasMore = page < totalPages;
      
      // 确保每个院校都有完整的数据结构和标签列表，并转换字段名
      const processedUniversities = universities.map(uni => {
        const tagList = [];
        if (uni.is_985) tagList.push('985');
        if (uni.is_211) tagList.push('211');
        if (uni.is_double_first_class) tagList.push('双一流');
        if (uni.school_nature) tagList.push(uni.school_nature);
        
        return {
          id: uni.id,
          schoolName: uni.school_name || '',
          schoolIdCode: uni.school_id_code || '',
          supervisor: uni.supervisor || '',
          location: uni.location || '',
          schoolLevel: uni.level || uni.school_level || '',
          schoolType: uni.school_type || uni.school_nature || '',
          logoUrl: uni.logo_url || '',
          website: uni.website || '',
          address: uni.address || '',
          phone: uni.phone || '',
          is985: uni.is_985 || false,
          is211: uni.is_211 || false,
          isDoubleFirstClass: uni.is_double_first_class || false,
          schoolNature: uni.school_nature || '',
          introduction: uni.introduction || '',
          tagList: tagList,
          wechatName: uni.wechat_name || '',
          wechatId: uni.wechat_id || '',
          weiboName: uni.weibo_name || '',
          weiboId: uni.weibo_id || '',
          baijiaName: uni.baijia_name || '',
          baijiaId: uni.baijia_id || '',
          videoName: uni.video_name || '',
          videoId: uni.video_id || ''
        };
      });
      
      this.setData({
        universities: append ? this.data.universities.concat(processedUniversities) : processedUniversities,
        total: total,
        hasMore: hasMore,
        loading: false,
        networkError: false
      });
      
      wx.hideLoading();
      
      if (!append && universities.length === 0) {
        wx.showToast({
          title: '暂无符合条件的院校',
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
    console.log('跳转到院校详情，ID:', id);
    wx.navigateTo({
      url: `/packageB/pages/university/detail?id=${id}`
    });
  },

  goToEnrollment() {
    wx.navigateTo({
      url: '/pages/enrollment/enrollment'
    });
  }
});
