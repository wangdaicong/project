// packageB/pages/university/detail.js
const request = require('../../../utils/request');
const app = getApp();

Page({
  data: {
    id: null,
    university: {},
    majors: [],
    allMajors: [], // 保存所有专业
    majorCategories: [], // 专业分类列表
    selectedCategory: '全部', // 当前选中的分类
    isFavorite: false,
    isInCompare: false,
    logoText: '',
    logoGradient: '',
    currentTab: 0,
    zhangxuefeng: {}
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadUniversityDetail();
      this.loadMajors();
      this.loadZhangxuefengAnalysis();
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
      
      // 处理数据库字段到前端字段的映射（14个爬取字段）
      const university = {
        id: res.id,
        name: res.school_name || res.name || '未知院校',
        schoolName: res.school_name || res.name || '未知院校',
        province: res.province || res.location || '',
        city: res.city || '',
        location: res.location || (res.province ? `${res.province} ${res.city || ''}`.trim() : ''),
        level: res.school_level || res.level || '',
        type: res.school_nature || res.type || '',
        // 14个爬取字段
        supervisor: res.supervisor || '',  // 1. 主管部门
        schoolNature: res.school_type || res.school_nature || '',  // 2. 院校特性（兼容两种字段名）
        logoUrl: res.logo_url || '',  // 3. Logo
        website: res.website || res.official_website || '',  // 4. 官方网址
        enrollmentWebsite: res.enrollment_website || '',  // 5. 招生网址
        address: res.address || res.detailed_address || '',  // 6. 详细地址
        phone: res.phone || res.official_phone || '',  // 7. 官方电话
        introduction: res.introduction || res.school_introduction || '暂无简介',  // 8. 学校简介
        features: res.school_features || res.features || '暂无特色介绍',  // 9. 办学特色
        wechatName: res.wechat_name || '',  // 10. 微信公众号名称
        wechatId: res.wechat_id || '',  // 11. 微信公众号ID
        weiboName: res.weibo_name || '',  // 12. 微博名称
        weiboId: res.weibo_id || '',  // 13. 微博ID
        baijiaName: res.baijia_name || '',  // 14. 百家号名称
        baijiaId: res.baijia_id || '',  // 15. 百家号ID
        baijiaName: res.baijia_name || '',  // 13. 百家号名称
        baijiaId: res.baijia_id || '',  // 14. 百家号ID
        videoName: res.video_name || '',  // 额外：视频号名称
        videoId: res.video_id || '',  // 额外：视频号ID
        is985: res.is_985 || false,
        is211: res.is_211 || false,
        isDoubleFirstClass: res.is_double_first_class || false,
        minScore: null,
        maxScore: null
      };
      
      // 生成logo文字和渐变色
      const logoData = this.generateLogoData(university.name);
      
      this.setData({ 
        university: university,
        logoText: logoData.text,
        logoGradient: logoData.gradient
      });
      wx.setNavigationBarTitle({
        title: university.name
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
      console.log('[DEBUG] 专业列表原始数据:', res);
      
      // 处理专业数据：确保tags字段存在，category为空或"-"时归为"其他"，并拆分tags为数组
      const majors = (res || []).map(major => {
        const category = (major.category && major.category !== '-') ? major.category : '其他';
        const tags = major.tags || '';
        const tagsArray = tags ? tags.split(',').map(t => t.trim()).filter(t => t) : [];
        return {
          ...major,
          category: category,
          tags: tags,
          tagsArray: tagsArray
        };
      });
      
      console.log('[DEBUG] 处理后的专业数据:', majors);
      console.log('[DEBUG] 第一个专业tags:', majors[0]?.tags);
      console.log('[DEBUG] 第一个专业category:', majors[0]?.category);
      
      // 提取所有分类（包括"其他"）
      const categories = ['全部'];
      const categorySet = new Set();
      majors.forEach(major => {
        categorySet.add(major.category);
      });
      // 排序，但"其他"放在最后
      const sortedCategories = Array.from(categorySet).sort((a, b) => {
        if (a === '其他') return 1;
        if (b === '其他') return -1;
        return a.localeCompare(b);
      });
      categories.push(...sortedCategories);
      
      this.setData({ 
        allMajors: majors,
        majors: majors,
        majorCategories: categories,
        selectedCategory: '全部'
      });
    }).catch(err => {
      console.error('加载专业列表失败', err);
    });
  },

  // 切换专业分类
  switchMajorCategory(e) {
    const category = e.currentTarget.dataset.category;
    const filteredMajors = category === '全部' 
      ? this.data.allMajors 
      : this.data.allMajors.filter(m => m.category === category);
    
    this.setData({
      selectedCategory: category,
      majors: filteredMajors
    });
  },

  // 加载张雪峰式分析
  loadZhangxuefengAnalysis() {
    console.log('[DEBUG] 开始加载张雪峰分析，ID:', this.data.id);
    
    request.get(`/university/zhangxuefeng/${this.data.id}`).then(res => {
      console.log('[DEBUG] API返回数据:', res);
      console.log('[DEBUG] 数据类型:', typeof res);
      console.log('[DEBUG] historical_affiliation:', res?.historical_affiliation);
      console.log('[DEBUG] industry_recognition:', res?.industry_recognition);
      
      this.setData({ zhangxuefeng: res || {} }, () => {
        console.log('[DEBUG] setData完成，当前zhangxuefeng:', this.data.zhangxuefeng);
      });
    }).catch(err => {
      console.error('[DEBUG] 加载失败:', err);
      this.setData({ zhangxuefeng: {} });
    });
  },

  // 切换Tab
  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab: index });
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

  openEnrollmentWebsite() {
    const website = this.data.university.enrollmentWebsite;
    if (!website) {
      wx.showToast({
        title: '暂无招生网址',
        icon: 'none'
      });
      return;
    }
    wx.setClipboardData({
      data: website,
      success() {
        wx.showToast({
          title: '招生网址已复制',
          icon: 'success'
        });
      }
    });
  },

  goToMajorDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/packageB/pages/major/detail?id=${id}`
    });
  },

  // 查看分数线趋势
  viewScoreTrend() {
    wx.navigateTo({
      url: `/packageB/pages/score-trend/score-trend?id=${this.data.id}&name=${this.data.university.name}`
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