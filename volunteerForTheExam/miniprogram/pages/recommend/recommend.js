const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    userScore: 0,
    userProvince: '全国',
    loading: true,
    recommendations: []
  },

  onLoad(options) {
    const score = app.globalData.userScore;
    const province = app.globalData.userProvince || '全国';
    
    if (!score) {
      wx.showToast({
        title: '请先输入分数',
        icon: 'none'
      });
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }, 1500);
      return;
    }

    this.setData({
      userScore: score,
      userProvince: province
    });

    this.loadRecommendations();
  },

  loadRecommendations() {
    this.setData({ loading: true });

    const { userScore, userProvince } = this.data;
    
    console.log('开始加载推荐，分数:', userScore, '省份:', userProvince);

    // 尝试从后端获取推荐
    request.get(`/university/recommend?score=${userScore}&province=${userProvince}`)
      .then(res => {
        console.log('后端返回数据:', res);
        this.setData({
          recommendations: res || [],
          loading: false
        });
        
        // 为每个推荐生成AI理由
        this.generateReasons();
      })
      .catch(err => {
        console.error('加载推荐失败，使用模拟数据', err);
        // 使用模拟数据
        const mockData = this.getMockRecommendations(userScore);
        console.log('模拟数据:', mockData);
        this.setData({
          recommendations: mockData,
          loading: false
        });
        
        // 为每个推荐生成AI理由
        this.generateReasons();
      });
  },

  getMockRecommendations(score) {
    console.log('生成模拟数据，分数:', score);
    
    // 优化推荐策略：冲刺3个 + 稳妥4个 + 保底3个 = 共10个
    // 根据分数区间返回精准推荐
    
    if (score >= 680) {
      // 超高分段（680+）：顶尖名校
      return [
        // 冲刺档（3个）- 分数高于考生30-40分
        { id: 1, name: '清华大学', level: '985/211/双一流', province: '北京', city: '北京', type: '理工', ranking: 1, minScore: 688, probability: '冲刺', probabilityClass: 'rush' },
        { id: 2, name: '北京大学', level: '985/211/双一流', province: '北京', city: '北京', type: '综合', ranking: 2, minScore: 685, probability: '冲刺', probabilityClass: 'rush' },
        { id: 3, name: '复旦大学', level: '985/211/双一流', province: '上海', city: '上海', type: '综合', ranking: 3, minScore: 683, probability: '冲刺', probabilityClass: 'rush' },
        // 稳妥档（4个）- 分数接近考生±10分
        { id: 4, name: '上海交通大学', level: '985/211/双一流', province: '上海', city: '上海', type: '综合', ranking: 4, minScore: 680, probability: '稳妥', probabilityClass: 'stable' },
        { id: 5, name: '浙江大学', level: '985/211/双一流', province: '浙江', city: '杭州', type: '综合', ranking: 5, minScore: 678, probability: '稳妥', probabilityClass: 'stable' },
        { id: 6, name: '中国科学技术大学', level: '985/211/双一流', province: '安徽', city: '合肥', type: '理工', ranking: 6, minScore: 675, probability: '稳妥', probabilityClass: 'stable' },
        { id: 7, name: '南京大学', level: '985/211/双一流', province: '江苏', city: '南京', type: '综合', ranking: 7, minScore: 672, probability: '稳妥', probabilityClass: 'stable' },
        // 保底档（3个）- 分数低于考生20-30分
        { id: 8, name: '中国人民大学', level: '985/211/双一流', province: '北京', city: '北京', type: '综合', ranking: 8, minScore: 665, probability: '保底', probabilityClass: 'safe' },
        { id: 9, name: '北京航空航天大学', level: '985/211/双一流', province: '北京', city: '北京', type: '理工', ranking: 9, minScore: 660, probability: '保底', probabilityClass: 'safe' },
        { id: 10, name: '同济大学', level: '985/211/双一流', province: '上海', city: '上海', type: '理工', ranking: 10, minScore: 655, probability: '保底', probabilityClass: 'safe' }
      ];
    } else if (score >= 630) {
      // 高分段（630-679）：985/211名校
      return [
        { id: 11, name: '武汉大学', level: '985/211/双一流', province: '湖北', city: '武汉', type: '综合', ranking: 11, minScore: 665, probability: '冲刺', probabilityClass: 'rush' },
        { id: 12, name: '华中科技大学', level: '985/211/双一流', province: '湖北', city: '武汉', type: '综合', ranking: 12, minScore: 660, probability: '冲刺', probabilityClass: 'rush' },
        { id: 13, name: '西安交通大学', level: '985/211/双一流', province: '陕西', city: '西安', type: '综合', ranking: 13, minScore: 655, probability: '冲刺', probabilityClass: 'rush' },
        { id: 14, name: '哈尔滨工业大学', level: '985/211/双一流', province: '黑龙江', city: '哈尔滨', type: '理工', ranking: 14, minScore: 640, probability: '稳妥', probabilityClass: 'stable' },
        { id: 15, name: '北京师范大学', level: '985/211/双一流', province: '北京', city: '北京', type: '师范', ranking: 15, minScore: 638, probability: '稳妥', probabilityClass: 'stable' },
        { id: 16, name: '中山大学', level: '985/211/双一流', province: '广东', city: '广州', type: '综合', ranking: 16, minScore: 635, probability: '稳妥', probabilityClass: 'stable' },
        { id: 17, name: '四川大学', level: '985/211/双一流', province: '四川', city: '成都', type: '综合', ranking: 17, minScore: 632, probability: '稳妥', probabilityClass: 'stable' },
        { id: 18, name: '厦门大学', level: '985/211/双一流', province: '福建', city: '厦门', type: '综合', ranking: 18, minScore: 620, probability: '保底', probabilityClass: 'safe' },
        { id: 19, name: '天津大学', level: '985/211/双一流', province: '天津', city: '天津', type: '理工', ranking: 19, minScore: 615, probability: '保底', probabilityClass: 'safe' },
        { id: 20, name: '东南大学', level: '985/211/双一流', province: '江苏', city: '南京', type: '综合', ranking: 20, minScore: 610, probability: '保底', probabilityClass: 'safe' }
      ];
    } else if (score >= 580) {
      // 中高分段（580-629）：211及优质双一流
      return [
        { id: 21, name: '北京理工大学', level: '985/211/双一流', province: '北京', city: '北京', type: '理工', ranking: 21, minScore: 615, probability: '冲刺', probabilityClass: 'rush' },
        { id: 22, name: '华东师范大学', level: '985/211/双一流', province: '上海', city: '上海', type: '师范', ranking: 22, minScore: 610, probability: '冲刺', probabilityClass: 'rush' },
        { id: 23, name: '大连理工大学', level: '985/211/双一流', province: '辽宁', city: '大连', type: '理工', ranking: 23, minScore: 605, probability: '冲刺', probabilityClass: 'rush' },
        { id: 24, name: '郑州大学', level: '211/双一流', province: '河南', city: '郑州', type: '综合', ranking: 45, minScore: 590, probability: '稳妥', probabilityClass: 'stable' },
        { id: 25, name: '苏州大学', level: '211/双一流', province: '江苏', city: '苏州', type: '综合', ranking: 50, minScore: 588, probability: '稳妥', probabilityClass: 'stable' },
        { id: 26, name: '南京航空航天大学', level: '211/双一流', province: '江苏', city: '南京', type: '理工', ranking: 55, minScore: 585, probability: '稳妥', probabilityClass: 'stable' },
        { id: 27, name: '西南大学', level: '211/双一流', province: '重庆', city: '重庆', type: '综合', ranking: 60, minScore: 582, probability: '稳妥', probabilityClass: 'stable' },
        { id: 28, name: '河南大学', level: '双一流', province: '河南', city: '开封', type: '综合', ranking: 88, minScore: 565, probability: '保底', probabilityClass: 'safe' },
        { id: 29, name: '深圳大学', level: '普通本科', province: '广东', city: '深圳', type: '综合', ranking: 95, minScore: 560, probability: '保底', probabilityClass: 'safe' },
        { id: 30, name: '华南师范大学', level: '211/双一流', province: '广东', city: '广州', type: '师范', ranking: 70, minScore: 555, probability: '保底', probabilityClass: 'safe' }
      ];
    } else if (score >= 520) {
      // 中分段（520-579）：省属重点及优质本科
      return [
        { id: 31, name: '南京邮电大学', level: '双一流', province: '江苏', city: '南京', type: '理工', ranking: 75, minScore: 568, probability: '冲刺', probabilityClass: 'rush' },
        { id: 32, name: '杭州电子科技大学', level: '普通本科', province: '浙江', city: '杭州', type: '理工', ranking: 80, minScore: 562, probability: '冲刺', probabilityClass: 'rush' },
        { id: 33, name: '河南大学', level: '双一流', province: '河南', city: '开封', type: '综合', ranking: 88, minScore: 555, probability: '冲刺', probabilityClass: 'rush' },
        { id: 34, name: '宁波大学', level: '双一流', province: '浙江', city: '宁波', type: '综合', ranking: 90, minScore: 540, probability: '稳妥', probabilityClass: 'stable' },
        { id: 35, name: '扬州大学', level: '普通本科', province: '江苏', city: '扬州', type: '综合', ranking: 95, minScore: 535, probability: '稳妥', probabilityClass: 'stable' },
        { id: 36, name: '江苏大学', level: '普通本科', province: '江苏', city: '镇江', type: '综合', ranking: 100, minScore: 530, probability: '稳妥', probabilityClass: 'stable' },
        { id: 37, name: '浙江工业大学', level: '普通本科', province: '浙江', city: '杭州', type: '理工', ranking: 105, minScore: 528, probability: '稳妥', probabilityClass: 'stable' },
        { id: 38, name: '南京工业大学', level: '普通本科', province: '江苏', city: '南京', type: '理工', ranking: 110, minScore: 515, probability: '保底', probabilityClass: 'safe' },
        { id: 39, name: '青岛大学', level: '普通本科', province: '山东', city: '青岛', type: '综合', ranking: 115, minScore: 510, probability: '保底', probabilityClass: 'safe' },
        { id: 40, name: '山东师范大学', level: '普通本科', province: '山东', city: '济南', type: '师范', ranking: 120, minScore: 505, probability: '保底', probabilityClass: 'safe' }
      ];
    } else if (score >= 450) {
      // 一般本科段（450-519）：普通本科
      return [
        { id: 41, name: '浙江师范大学', level: '普通本科', province: '浙江', city: '金华', type: '师范', ranking: 125, minScore: 510, probability: '冲刺', probabilityClass: 'rush' },
        { id: 42, name: '温州大学', level: '普通本科', province: '浙江', city: '温州', type: '综合', ranking: 135, minScore: 505, probability: '冲刺', probabilityClass: 'rush' },
        { id: 43, name: '江西师范大学', level: '普通本科', province: '江西', city: '南昌', type: '师范', ranking: 140, minScore: 498, probability: '冲刺', probabilityClass: 'rush' },
        { id: 44, name: '安徽师范大学', level: '普通本科', province: '安徽', city: '芜湖', type: '师范', ranking: 145, minScore: 485, probability: '稳妥', probabilityClass: 'stable' },
        { id: 45, name: '湖北大学', level: '普通本科', province: '湖北', city: '武汉', type: '综合', ranking: 150, minScore: 480, probability: '稳妥', probabilityClass: 'stable' },
        { id: 46, name: '长沙理工大学', level: '普通本科', province: '湖南', city: '长沙', type: '理工', ranking: 155, minScore: 475, probability: '稳妥', probabilityClass: 'stable' },
        { id: 47, name: '浙江财经大学', level: '普通本科', province: '浙江', city: '杭州', type: '财经', ranking: 160, minScore: 470, probability: '稳妥', probabilityClass: 'stable' },
        { id: 48, name: '重庆工商大学', level: '普通本科', province: '重庆', city: '重庆', type: '财经', ranking: 170, minScore: 455, probability: '保底', probabilityClass: 'safe' },
        { id: 49, name: '西安石油大学', level: '普通本科', province: '陕西', city: '西安', type: '理工', ranking: 180, minScore: 450, probability: '保底', probabilityClass: 'safe' },
        { id: 50, name: '桂林电子科技大学', level: '普通本科', province: '广西', city: '桂林', type: '理工', ranking: 185, minScore: 445, probability: '保底', probabilityClass: 'safe' }
      ];
    } else {
      // 专科段（450以下）：优质高职
      return [
        { id: 51, name: '深圳职业技术学院', level: '高职专科', province: '广东', city: '深圳', type: '综合', ranking: 1, minScore: 430, probability: '冲刺', probabilityClass: 'rush' },
        { id: 52, name: '南京工业职业技术大学', level: '高职专科', province: '江苏', city: '南京', type: '理工', ranking: 2, minScore: 420, probability: '冲刺', probabilityClass: 'rush' },
        { id: 53, name: '金华职业技术学院', level: '高职专科', province: '浙江', city: '金华', type: '综合', ranking: 3, minScore: 410, probability: '冲刺', probabilityClass: 'rush' },
        { id: 54, name: '无锡职业技术学院', level: '高职专科', province: '江苏', city: '无锡', type: '理工', ranking: 5, minScore: 390, probability: '稳妥', probabilityClass: 'stable' },
        { id: 55, name: '广东轻工职业技术学院', level: '高职专科', province: '广东', city: '广州', type: '理工', ranking: 6, minScore: 385, probability: '稳妥', probabilityClass: 'stable' },
        { id: 56, name: '北京电子科技职业学院', level: '高职专科', province: '北京', city: '北京', type: '理工', ranking: 8, minScore: 380, probability: '稳妥', probabilityClass: 'stable' },
        { id: 57, name: '陕西工业职业技术学院', level: '高职专科', province: '陕西', city: '咸阳', type: '理工', ranking: 10, minScore: 375, probability: '稳妥', probabilityClass: 'stable' },
        { id: 58, name: '杨凌职业技术学院', level: '高职专科', province: '陕西', city: '杨凌', type: '农林', ranking: 12, minScore: 360, probability: '保底', probabilityClass: 'safe' },
        { id: 59, name: '天津职业大学', level: '高职专科', province: '天津', city: '天津', type: '综合', ranking: 15, minScore: 350, probability: '保底', probabilityClass: 'safe' },
        { id: 60, name: '重庆电子工程职业学院', level: '高职专科', province: '重庆', city: '重庆', type: '理工', ranking: 18, minScore: 340, probability: '保底', probabilityClass: 'safe' }
      ];
    }
  },

  generateReasons() {
    const { recommendations, userScore } = this.data;
    
    console.log('开始生成AI理由，推荐数量:', recommendations.length);
    
    // 为每个推荐院校生成AI理由
    recommendations.forEach((item, index) => {
      console.log(`生成第${index + 1}个推荐的理由:`, item.name);
      this.generateSingleReason(item, index);
    });
  },

  generateSingleReason(university, index) {
    const { userScore } = this.data;
    
    // 调用DeepSeek API生成推荐理由
    const prompt = `作为高考志愿填报专家，请为考生分析推荐理由。
考生分数：${userScore}分
推荐院校：${university.name}
院校层次：${university.level}
院校类型：${university.type}
最低录取分：${university.minScore}分
录取概率：${university.probability}

请从以下角度简要分析（100字以内）：
1. 分数匹配度
2. 院校优势
3. 报考建议`;

    request.post('/ai/analyze', {
      prompt: prompt,
      maxTokens: 200
    }).then(res => {
      const reason = res.content || res;
      const key = `recommendations[${index}].reason`;
      this.setData({
        [key]: reason
      });
    }).catch(err => {
      console.error('生成推荐理由失败', err);
      // 使用默认理由
      const defaultReason = this.getDefaultReason(university, userScore);
      const key = `recommendations[${index}].reason`;
      this.setData({
        [key]: defaultReason
      });
    });
  },

  getDefaultReason(university, userScore) {
    const scoreDiff = userScore - university.minScore;
    let reason = '';

    // 根据院校类型和层次生成更专业的分析
    const getUniversityFeature = (uni) => {
      if (uni.level.includes('985')) return '国内顶尖名校，学科实力雄厚，就业竞争力强';
      if (uni.level.includes('211')) return '国家重点建设高校，学科优势明显，社会认可度高';
      if (uni.level.includes('双一流')) return '国家"双一流"建设高校，特色学科实力突出';
      if (uni.level.includes('高职专科')) return '国家示范性高职院校，技能培养扎实，就业率高';
      return '办学特色鲜明，专业建设完善';
    };

    const getCityAdvantage = (city) => {
      const cityInfo = {
        '北京': '首都，政治文化中心，实习就业机会多，视野开阔',
        '上海': '经济金融中心，国际化程度高，就业薪资水平领先',
        '广州': '经济发达，就业机会多，生活成本适中',
        '深圳': '创新创业之都，高新技术产业发达，薪资待遇优厚',
        '杭州': '互联网产业发达，创业氛围浓厚，生活环境优美',
        '南京': '科教资源丰富，历史文化底蕴深厚，就业环境好',
        '武汉': '九省通衢，科教实力强，生活成本较低',
        '成都': '西部中心城市，生活舒适，发展潜力大',
        '西安': '科教资源丰富，历史文化名城，西部重要城市'
      };
      return cityInfo[city] || `地处${city}，区域发展前景良好`;
    };

    if (university.probability === '冲刺') {
      if (scoreDiff < 0) {
        reason = `【冲刺档】\n\n` +
          `📊 分数分析：${university.name}往年最低录取分${university.minScore}分，您的分数${userScore}分，低${Math.abs(scoreDiff)}分。虽然分数略低，但仍有冲刺机会。\n\n` +
          `🏫 院校优势：${getUniversityFeature(university)}。${getCityAdvantage(university.city)}。\n\n` +
          `💡 报考策略：\n` +
          `• 专业选择：建议选择该校相对冷门但实力不错的专业，避开热门专业竞争\n` +
          `• 调剂意愿：务必勾选"服从专业调剂"，大幅提高录取概率\n` +
          `• 志愿搭配：此为冲刺志愿，必须配合稳妥和保底志愿，确保录取安全\n` +
          `• 位次参考：重点关注往年录取位次，比最低分更有参考价值`;
      } else {
        reason = `【冲刺档】\n\n` +
          `📊 分数分析：${university.name}往年最低录取分${university.minScore}分，您的分数${userScore}分，高${scoreDiff}分。有一定录取机会，但存在不确定性。\n\n` +
          `🏫 院校优势：${getUniversityFeature(university)}。${getCityAdvantage(university.city)}。\n\n` +
          `💡 报考策略：\n` +
          `• 专业选择：可选择该校优势专业，但需关注专业录取分数线\n` +
          `• 位次分析：重点对比往年录取位次，确认是否在合理范围内\n` +
          `• 大小年规律：了解该校录取分数大小年波动情况\n` +
          `• 志愿搭配：作为冲刺志愿，后续必须填报稳妥院校保底`;
      }
    } else if (university.probability === '稳妥') {
      reason = `【稳妥档】\n\n` +
        `📊 分数分析：${university.name}往年最低录取分${university.minScore}分，您的分数${userScore}分，高${scoreDiff}分。录取概率较大，可作为主要目标院校。\n\n` +
        `🏫 院校优势：${getUniversityFeature(university)}。该校${university.type}类专业实力强劲，${getCityAdvantage(university.city)}。\n\n` +
        `💡 报考策略：\n` +
        `• 专业选择：可大胆选择心仪专业，包括该校王牌特色专业\n` +
        `• 就业前景：该校毕业生就业率高，${university.city}地区就业资源丰富\n` +
        `• 发展空间：可重点考虑该校保研率、考研氛围、深造机会\n` +
        `• 志愿定位：建议作为主要目标院校，录取把握较大`;
    } else {
      reason = `【保底档】\n\n` +
        `📊 分数分析：${university.name}往年最低录取分${university.minScore}分，您的分数${userScore}分，高${scoreDiff}分。录取把握很大，可确保有学可上。\n\n` +
        `🏫 院校优势：${getUniversityFeature(university)}。${getCityAdvantage(university.city)}。\n\n` +
        `💡 报考策略：\n` +
        `• 专业选择：可完全按照兴趣选择喜欢的专业，不必担心录取问题\n` +
        `• 发展规划：重点考虑专业发展前景和个人兴趣匹配度\n` +
        `• 保底作用：确保至少有一所保底院校录取，避免滑档风险\n` +
        `• 未来发展：该校${university.type}类专业就业稳定，可考虑专升本或考研深造`;
    }

    return reason;
  },

  regenerate() {
    wx.showLoading({ title: '重新推荐中...' });
    this.loadRecommendations();
    setTimeout(() => {
      wx.hideLoading();
    }, 1000);
  },

  goToUniversityDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/university/detail?id=${id}`
    });
  },

  goToUniversityPage() {
    wx.switchTab({
      url: '/pages/university/university'
    });
  }
});
