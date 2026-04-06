/**
 * CDN加速配置
 * 用于图片、静态资源的CDN加速
 */

// CDN域名配置（可根据实际情况修改）
const CDN_CONFIG = {
  // 主CDN域名
  primary: 'https://cdn.example.com',
  
  // 备用CDN域名
  fallback: 'https://cdn2.example.com',
  
  // 是否启用CDN
  enabled: false, // 开发环境设为false，生产环境设为true
  
  // 图片压缩参数
  imageQuality: {
    thumbnail: '?imageView2/1/w/200/h/200/q/75', // 缩略图
    medium: '?imageView2/1/w/800/h/800/q/85',     // 中等尺寸
    large: '?imageView2/1/w/1200/h/1200/q/90'     // 大图
  }
};

/**
 * 获取CDN图片URL
 * @param {String} path 图片路径
 * @param {String} size 图片尺寸：thumbnail/medium/large
 * @returns {String} CDN URL
 */
function getCdnImageUrl(path, size = 'medium') {
  if (!path) return '';
  
  // 如果未启用CDN，返回原路径
  if (!CDN_CONFIG.enabled) {
    return path;
  }
  
  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // 构建CDN URL
  const cdnUrl = `${CDN_CONFIG.primary}${path}`;
  const quality = CDN_CONFIG.imageQuality[size] || '';
  
  return cdnUrl + quality;
}

/**
 * 获取CDN静态资源URL
 * @param {String} path 资源路径
 * @returns {String} CDN URL
 */
function getCdnStaticUrl(path) {
  if (!path) return '';
  
  if (!CDN_CONFIG.enabled) {
    return path;
  }
  
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  return `${CDN_CONFIG.primary}${path}`;
}

/**
 * 图片加载失败时使用备用CDN
 * @param {Event} e 错误事件
 */
function onImageError(e) {
  const img = e.currentTarget;
  const src = img.dataset.src;
  
  if (src && !img.dataset.fallbackTried) {
    // 标记已尝试备用CDN
    img.dataset.fallbackTried = 'true';
    
    // 使用备用CDN
    const fallbackUrl = src.replace(CDN_CONFIG.primary, CDN_CONFIG.fallback);
    img.src = fallbackUrl;
  } else {
    // 使用默认占位图
    img.src = '/images/placeholder.png';
  }
}

/**
 * 预加载图片
 * @param {Array} urls 图片URL数组
 * @returns {Promise} 预加载Promise
 */
function preloadImages(urls) {
  return Promise.all(
    urls.map(url => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(url);
        img.onerror = () => reject(url);
        img.src = url;
      });
    })
  );
}

/**
 * 图片懒加载
 * @param {String} selector 图片选择器
 */
function lazyLoadImages(selector = '.lazy-image') {
  const images = document.querySelectorAll(selector);
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        const src = img.dataset.src;
        
        if (src) {
          img.src = getCdnImageUrl(src);
          observer.unobserve(img);
        }
      }
    });
  });
  
  images.forEach(img => observer.observe(img));
}

module.exports = {
  getCdnImageUrl,
  getCdnStaticUrl,
  onImageError,
  preloadImages,
  lazyLoadImages,
  CDN_CONFIG
};
