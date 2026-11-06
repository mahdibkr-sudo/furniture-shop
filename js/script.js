/* ==============================
   Data Structures for the Site
   ============================== */
const products = [
  {
    id: 1,
    name: "مبل راحتی مدل موچی",
    category: "مبل",
    description: "مدل موچی با طراحی نرم، گرد و مینیمال، انتخابی ایده‌آل برای فضاهای مدرن و امروزی است. فرم یکپارچه و منحنی بدنه در کنار نشیمن راحت و پشتی‌های حجیم، ظاهری گرم و آرامش‌بخش به این مدل بخشیده است. پایه چوبی تیره نیز در کنار پارچه کرم‌رنگ، ترکیبی شیک و متعادل ایجاد می‌کند.",
    price: 0,
    material: "چوب راش + پارچه بخمله",
    images: [
      "images/product-1.webp"
    ],
    specs: {
      "تعداد نشیمن": "۷",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 2,
    name: "مبل راحتی مدل رادیس",
    category: "مبل",
    description: "مدل رادیس با طراحی منحنی و ظاهری مدرن، ترکیبی از نرمی، راحتی و زیبایی را ارائه می‌دهد. نشیمن عمیق و فرم ارگونومیک آن، آرامش و راحتی را برای استفاده روزمره فراهم می‌کند.",
    price: 0,
    material: "—",
    images: [
      "images/product-2.webp"
    ],
    specs: {
      "تعداد نشیمن": "۷",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 3,
    name: "مبل راحتی مدل ال تانیش",
    category: "مبل",
    description: "این مدل با طراحی L شکل و شزلون‌دار، ترکیبی از راحتی و ظاهری مدرن و شیک را ارائه می‌دهد. نشیمن‌های عمیق و نرم، پشتی‌های حجیم و دسته‌های ظریف، فضای مناسبی برای استراحت و استفاده روزمره ایجاد کرده‌اند. پایه و نوار چوبی با رنگ گرم نیز در کنار پارچه طوسی، جلوه‌ای مینیمال و امروزی به مبل بخشیده است.",
    price: 0,
    material: "—",
    images: [
      "images/product-3.webp"
    ],
    specs: {
      "تعداد نشیمن": "۷",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 4,
    name: "میز جلو مبلی لونا",
    category: "میز جلو مبلی",
    description: "طراحی گرد و مدرن با فرم‌های هماهنگ و لایه‌ای، ساخته‌شده از چوب با بافت طبیعی و پایه‌های ظریف. ترکیبی از سادگی، زیبایی و کاربردی بودن برای دکوراسیون‌های مدرن.",
    price: 0,
    material: "—",
    images: [
      "images/table-1.webp"
    ],
    specs: {
      "جنس": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 5,
    name: "میز جلو مبلی رما",
    category: "میز جلو مبلی",
    description: "ترکیبی متفاوت از میز گرد چوبی و میز عثمانی پارچه‌ای با صفحه‌های لبه‌دار و طراحی مینیمال. هماهنگی چوب گرم و پارچه روشن، جلوه‌ای شیک و امروزی به فضای پذیرایی می‌دهد.",
    price: 0,
    material: "—",
    images: [
      "images/table-2.webp"
    ],
    specs: {
      "جنس": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 6,
    name: "مبل راحتی مدل ایفوریا",
    category: "مبل",
    description: "طراحی مدرن و مینیمال با نشیمن عمیق و راحت، کوسن‌های بزرگ و فرم ساده و یکپارچه. ترکیب مبل سه‌نفره با شزلون، فضای نشیمن جادار و کاربردی ایجاد کرده و پایه‌های چوبی جلوه‌ای گرم و شیک به آن می‌بخشد. مناسب برای دکوراسیون‌های مدرن و مینیمال.",
    price: 0,
    material: "—",
    images: [
      "images/product-4.webp"
    ],
    specs: {
      "تعداد نشیمن": "۷",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 7,
    name: "مبل تکنفره ست موچی",
    category: "مبل",
    description: "مبل تکی با طراحی مدرن و فرم گرد، انتخابی خاص و راحت برای دکوراسیون‌های امروزی است. قابلیت چرخش ۳۶۰ درجه این مدل، آزادی حرکت بیشتری ایجاد کرده و امکان تغییر جهت نشستن را به‌راحتی فراهم می‌کند. نشیمن نرم و جادار و طراحی ارگونومیک آن نیز راحتی مطلوبی برای استراحت روزمره فراهم می‌سازد.",
    price: 0,
    material: "—",
    images: [
      "images/product-5.webp"
    ],
    specs: {
      "تعداد نشیمن": "۱",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  },
  {
    id: 8,
    name: "مبل تکنفره ست ال تانیش",
    category: "مبل",
    description: "مبل تکی با طراحی مدرن و ظاهری مینیمال، ترکیبی از پارچه لطیف و پایه‌های چوبی خوش‌تراش است. فرم ارگونومیک پشتی و نشیمن، راحتی مناسبی برای استفاده روزمره فراهم می‌کند و طراحی ساده آن به‌راحتی با دکوراسیون‌های مختلف هماهنگ می‌شود.",
    price: 0,
    material: "—",
    images: [
      "images/product-6.webp"
    ],
    specs: {
      "تعداد نشیمن": "۱",
      "جنس بدنه": "چوب نراد طبیعی و چوب صنوبر کبوده",
      "پارچه": "دلخواه به سلیقه مشتری",
      "رنگ": "سلیقه مشتری",
      "گارانتی": "۲۴ ماه"
    }
  }
];

const blogPosts = [
  {
    id: 1,
    title: "۵ نکته مهم قبل از خرید مبل راحتی",
    category: "راهنمای خرید",
    excerpt: "قبل از انتخاب مبل راحتی، این پنج نکته را بدانید تا خریدی مطمئن و راحت داشته باشید؛ از اندازه‌گیری فضا تا کیفیت اسکلت و پارچه.",
    image: "images/blog-1.webp",
    date: "۱۴۰۳/۰۵/۱۰",
    author: "تیم محتوای میراکل",
    readTime: "۴ دقیقه",
    content: [
      "خرید مبل راحتی یکی از مهم‌ترین تصمیم‌های دکوراسیون خانه است؛ چرا که مبل، قلب پذیرایی شماست و سال‌ها همراه شما خواهد بود. پیش از خرید، این پنج نکته را حتماً در نظر بگیرید.",
      "۱. فضای خود را اندازه بگیرید: پیش از هر چیز، ابعاد دقیق پذیرایی را اندازه بگیرید و قابل عبور بودن مسیرها را فراموش نکنید. مبل بزرگ در فضای کوچک، حس خفگی ایجاد می‌کند و مبل کوچک در فضای بزرگ، حس خلوتی نامطلوب دارد.",
      "۲. کیفیت اسکلت را بررسی کنید: اسکلت چوبی خشک‌شده و اتصالات محکم، عمر مبل شما را چند برابر می‌کند. حتماً از جنس چوب و نحوه ساخت اسکلت سؤال کنید.",
      "۳. روی فوم و نشیمن حساس باشید: تراکم فوم نشیمن و پشتی، راحتی بلندمدت را تعیین می‌کند. نشستن‌های آزمایشی را در فروشگاه جدی بگیرید.",
      "۴. پارچه مناسب سبک زندگی: اگر کودک یا حیوان خانگی دارید، پارچه‌های ضدلک و مقاوم مثل مخمل و چرم مصنوعی انتخاب بهتری هستند.",
      "۵. رنگ را با دکوراسیون کلی هماهنگ کنید: رنگ‌های خنثی مثل کرم و طوسی ماندگارترند و با هر تغییر دکوراسیونی هماهنگ می‌شوند."
    ]
  },
  {
    id: 2,
    title: "چگونه مبل را با دکوراسیون خانه هماهنگ کنیم؟",
    category: "دکوراسیون",
    excerpt: "هماهنگی مبل با رنگ دیوارها، فرش و نورپردازی، جادوی پذیرایی شما را می‌سازد. با این اصول ساده، یک ترکیب حرفه‌ای بسازید.",
    image: "images/blog-2.webp",
    date: "۱۴۰۳/۰۴/۲۲",
    author: "تیم محتوای میراکل",
    readTime: "۳ دقیقه",
    content: [
      "هماهنگی مبل با بقیه اجزای پذیرایی، تفاوت بین یک خانه معمولی و یک خانه چشم‌نواز است. خوشبختانه رعایت چند اصل ساده برای رسیدن به این هماهنگی کافی است.",
      "قانون ۶۰-۳۰-۱۰ را به کار ببرید: ۶۰ درصد فضا رنگ اصلی (دیوارها و مبل)، ۳۰ درصد رنگ مکمل (فرش و پرده) و ۱۰ درصد رنگ تأکیدی (کوسن‌ها و اکسسوری).",
      "با بافت‌ها بازی کنید: ترکیب پارچه‌های مختلف مثل بخمله، کتان و چرم در کنار هم، عمق و جذابیت بصری ایجاد می‌کند؛ به شرطی که در یک خانواده رنگی بمانند.",
      "نورپردازی را فراموش نکنید: یک مبل زیبا زیر نور نامناسب دیده نمی‌شود. نور گرم و مکان‌دار، رنگ‌های قهوه‌ای و کرم مبلمان را هرچه زیباتر نشان می‌دهد.",
      "و در نهایت، تعادل را حفظ کنید: در کنار مبل‌های حجیم و پرحجم، از میزهای ظریف و سبک استفاده کنید تا فضا نفس بکشد."
    ]
  },
  {
    id: 3,
    title: "راهنمای نگهداری و مراقبت از مبلمان پارچه‌ای",
    category: "مراقبت",
    excerpt: "با چند عادت ساده، عمر مبل پارچه‌ای خود را چند برابر کنید؛ از جاروکردن هفتگی تا روش درست خشک کردن لکه‌ها.",
    image: "images/blog-3.webp",
    date: "۱۴۰۳/۰۳/۱۸",
    author: "تیم محتوای میراکل",
    readTime: "۳ دقیقه",
    content: [
      "مبلمان پارچه‌ای با نگهداری درست، سال‌ها مثل روز اول باقی می‌ماند. این راهنما را دنبال کنید تا سرمایه‌تان را حفظ کنید.",
      "جاروبرقی هفتگی: با نازل مخصوص مبل، هفته‌ای یک بار گردوغبار و ذرات بین درزها را بگیرید. این ساده‌ترین و مؤثرترین کار برای جلوگیری از کدر شدن پارچه است.",
      "لکه‌ها را فوری و درست پاک کنید: هرگز لکه را نمالید؛ با یک دستمال تمیز از بیرون به داخل لکه ضربه بزنید تا وارد الیاف نشود. برای لکه‌های چرب از کمی آب و صابون ملایم استفاده کنید.",
      "کوسن‌ها را برگردانید و جابجا کنید: چرخش هفتگی کوسن‌ها و بالشتک‌ها باعث می‌شود فوم به‌طور یکنواخت فرسوده شود و فرم مبل حفظ بماند.",
      "از نور مستقیم آفتاب دوری کنید: تابش مستقیم خورشید باعث کم‌رنگ شدن پارچه می‌شود. اگر پذیرایی پنجره بزرگ دارد، از پرچه نیمه‌شفاف استفاده کنید.",
      "نکته پایانی: سالی یک بار بخارشوی ملایم یا شست‌وشوی تخصصی، تازگی و طراوت مبل شما را بازمی‌گرداند."
    ]
  },
  {
    id: 4,
    title: "میز جلو مبلی؛ قلب تپنده پذیرایی شما",
    category: "دکوراسیون",
    excerpt: "میز جلو مبلی فقط یک سطح برای چای و کتاب نیست؛ عنصری که شخصیت پذیرایی شما را تعیین می‌کند. با اصول انتخاب آن آشنا شوید.",
    image: "images/blog-4.webp",
    date: "۱۴۰۳/۰۲/۰۵",
    author: "تیم محتوای میراکل",
    readTime: "۳ دقیقه",
    content: [
      "میز جلو مبلی کوچک‌ترین قطعه پذیرایی است اما بیشترین تأثیر را روی ظاهر کلی فضا دارد. انتخاب درست آن می‌تواند کل ترکیب را متحول کند.",
      "ارتفاع را رعایت کنید: ارتفاع استاندارد میز جلو مبلی باید هم‌تراز یا کمی پایین‌تر از نشیمن مبل باشد؛ حدوداً ۴۰ تا ۵۰ سانتی‌متر.",
      "فاصله‌گذاری ایده‌آل: بین میز و مبل باید ۳۰ تا ۴۵ سانتی‌متر فضا باشد؛ نه آن‌قدر نزدیک که حرکت سخت شود و نه آن‌قدر دور که به آن دسترسی نداشته باشید.",
      "جنس را با سبک خانه انتخاب کنید: میز چوبی برای فضای گرم و کلاسیک، میز شیشه‌ای یا فلزی برای فضای مدرن و مینیمال. ترکیب چوب و شیشه هم پاسخ همه‌کاره‌ای برای سبک‌های ترکیبی است.",
      "و از تزئین زیاده‌روی نکنید: یک کتاب قشنگ، یک شمعدان و یک گلدان کوچک کافی است؛ میز باید نفس بکشد."
    ]
  }
];

const teamMembers = [
  {
    name: "علی میرزای",
    role: "مؤسس و طراح اصلی",
    bio: "بیش از ۲۵ سال تجربه در طراحی دکوراسیون داخلی و تولید مبلمان میراکل.",
    image: "team1.jpg"
  },
  {
    name: "سارا رحمانی",
    role: " مدیر فنی تولید",
    bio: "سرپرستی فرآیند تولید از طرح تا تحویل نهایی، کنترل کیفیت.",
    image: "team2.jpg"
  },
  {
    name: "محمدحسین رضایی",
    role: " مهندس محاسباتی",
    bio: "بهینه‌سازی فرآیند‌های CAD و роботи‌هایی که به حرفه referred می‌شوند.",
    image: "team3.jpg"
  }
];

const faqItems = [
  {
    question: "قیمت‌های محصولات چگونه تعیین می‌شوند؟",
    answer: "قیمت‌گذاری بر اساس عواملی مانند مواد اولیه، پیچیدگی طراحی و گارانتی ارائه‌شده انجام می‌شود. برای اطلاع از قیمت دقیق هر مدل، با ما تماس بگیرید."
  },
  {
    question: "چه مدت زمان تولید و تحویل طول می‌کشد؟",
    answer: "به‌طور معمول تولید و تحویل سفارش شما بین ۷ تا ۱۰ روز کاری زمان می‌برد."
  },
  {
    question: "آیا امکان سفارشی‌سازی وجود دارد؟",
    answer: "بله، پارچه و رنگ محصولات بر اساس سلیقه شما قابل انتخاب است. برای سفارشی‌سازی، مستقیماً با شماره‌های تماس در ارتباط باشید."
  }
];

/* ======================================
   Helper Functions
   ====================================== */
function formatPrice(number) {
  return new Intl.NumberFormat('fa-IR', {
    style: 'currency',
    currency: 'IRR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(number);
}

function renderElement(tag, attrs = {}, ...children) {
  const el = document.createElement(tag);
  Object.entries(attrs).forEach(([key, value]) => {
    if (key === 'class') el.className = value;
    else if (key.startsWith('data-')) el.setAttribute(key, value);
    else el[key] = value;
  });
  const append = (child) => {
    if (child === null || child === undefined || child === false) return;
    if (Array.isArray(child)) { child.forEach(append); return; }
    if (typeof child === 'string' || typeof child === 'number') {
      el.appendChild(document.createTextNode(child));
    } else {
      el.appendChild(child);
    }
  };
  children.forEach(append);
  return el;
}

/* ==============================
   1. Populate Catalog Page (index.html)
   ============================== */
function populateProductCatalog() {
  const grid = document.getElementById('catalogGrid');
  if (!grid) return;
  // Cards already rendered statically in HTML — don't duplicate
  if (grid.children.length > 0) return;

  products.forEach(product => {
    const card = renderElement('div', { class: 'product-card reveal' }, [
      renderElement('div', { class: 'product-card-image' }, [
        renderElement('img', { src: product.images[0], alt: product.name, loading: 'lazy' })
      ]),
      renderElement('div', { class: 'product-card-content' }, [
        renderElement('div', { class: 'product-card-category' }, product.category),
        renderElement('h3', { class: 'product-card-title' }, product.name),
        renderElement('p', { class: 'product-card-description' }, product.description),
        renderElement('div', { class: 'product-card-meta' }, [
          renderElement('span', { class: 'product-card-price' },
            product.price > 0 ? formatPrice(product.price) : 'استعلام قیمت'),
          renderElement('a', {
            href: `product.html?id=${product.id}`,
            class: 'btn btn-primary product-card-view-btn'
          }, 'مشاهده')
        ])
      ])
    ]);
    card.addEventListener('click', (e) => {
      if (e.target.tagName !== 'A') {
        window.location.href = `product.html?id=${product.id}`;
      }
    });
    grid.appendChild(card);
  });
}


/* ==============================
   3. Populate Blog Listing (blog.html)
   ============================== */
function populateBlogListing() {
  const container = document.getElementById('blogPreviewGrid') || document.getElementById('blogListingGrid');
  if (!container) return;
  // Cards already rendered statically — don't duplicate
  if (container.children.length > 0) return;
  // On the homepage show only the 3 latest posts
  const posts = container.id === 'blogPreviewGrid' ? blogPosts.slice(0, 3) : blogPosts;

  posts.forEach(post => {
    const postCard = renderElement('div', { class: 'blog-card reveal' }, [
      renderElement('div', { class: 'blog-card-image' }, [
        renderElement('img', { src: post.image, alt: post.title, loading: 'lazy' }),
        renderElement('span', { class: 'blog-card-category' }, post.category)
      ]),
      renderElement('div', { class: 'blog-card-content' }, [
        renderElement('div', { class: 'blog-card-meta' }, [
          renderElement('span', {}, `📅 ${post.date}`),
          renderElement('span', {}, `⏱ ${post.readTime}`)
        ]),
        renderElement('h3', { class: 'blog-card-title' }, post.title),
        renderElement('p', { class: 'blog-card-excerpt' }, post.excerpt),
        renderElement('a', { href: `blog-post.html?id=${post.id}`, class: 'blog-card-link' }, 'ادامه خواندن…')
      ])
    ]);
    container.appendChild(postCard);
  });
}

/* ==============================
   4. Populate Blog Post Page (blog-post.html)
   ============================== */
function populateBlogPost() {
  const urlParams = new URLSearchParams(window.location.search);
  const id = parseInt(urlParams.get('id'), 10) || 1;
  const post = blogPosts.find(p => p.id === id) || blogPosts[0];

  document.title = `${post.title} | وبلاگ مبلمان میراکل`;

  // Fill hero elements
  const breadcrumbTitle = document.getElementById('postBreadcrumbTitle');
  if (breadcrumbTitle) breadcrumbTitle.textContent = post.title;
  const titleEl = document.getElementById('post-title');
  if (titleEl) titleEl.textContent = post.title;
  const categoryEl = document.getElementById('postCategory');
  if (categoryEl) categoryEl.textContent = post.category;
  const dateEl = document.getElementById('postDate');
  if (dateEl) dateEl.textContent = post.date;
  const readTimeEl = document.getElementById('postReadTime');
  if (readTimeEl) readTimeEl.textContent = post.readTime;
  const authorNameEl = document.getElementById('postAuthorName');
  if (authorNameEl) authorNameEl.textContent = post.author;
  const authorAvatarEl = document.getElementById('postAuthorAvatar');
  if (authorAvatarEl) authorAvatarEl.textContent = post.author.charAt(0);
  const imgEl = document.getElementById('postImage');
  if (imgEl) {
    imgEl.src = post.image;
    imgEl.alt = post.title;
  }

  // Fill main content
  const contentContainer = document.getElementById('postContent');
  if (contentContainer) {
    post.content.forEach(paragraph => {
      contentContainer.appendChild(renderElement('p', {}, paragraph));
    });
  }

  // Related posts (other posts)
  const relatedGrid = document.getElementById('relatedPostsGrid');
  if (relatedGrid) {
    blogPosts.filter(p => p.id !== post.id).slice(0, 3).forEach(p => {
      relatedGrid.appendChild(renderElement('div', { class: 'blog-card reveal' }, [
        renderElement('div', { class: 'blog-card-image' }, [
          renderElement('img', { src: p.image, alt: p.title, loading: 'lazy' }),
          renderElement('span', { class: 'blog-card-category' }, p.category)
        ]),
        renderElement('div', { class: 'blog-card-content' }, [
          renderElement('h3', { class: 'blog-card-title' }, p.title),
          renderElement('p', { class: 'blog-card-excerpt' }, p.excerpt),
          renderElement('a', { href: `blog-post.html?id=${p.id}`, class: 'blog-card-link' }, 'ادامه خواندن…')
        ])
      ]));
    });
  }
}

/* ==============================
   5. Populate Team Members (about.html)
   ============================== */
function populateTeam() {
  const grid = document.getElementById('teamGrid');
  if (!grid) return;

  teamMembers.forEach(member => {
    const card = renderElement('div', { class: 'project-card' }, [
      renderElement('div', { class: 'project-image' }, [
        renderElement('img', { src: member.image, alt: member.name })
      ]),
      renderElement('div', { class: 'project-info' }, [
        renderElement('h3', {}, member.name),
        renderElement('p', { class: 'project-title' }, member.role),
        renderElement('p', { class: 'project-description' }, member.bio)
      ])
    ]);
    grid.appendChild(card);
  });
}

/* ==============================
   6. Populate FAQ (blog.html or separate page)
   ============================== */
function populateFAQ() {
  const container = document.getElementById('faqContainer');
  if (!container) return;

  faqItems.forEach(item => {
    const itemDiv = renderElement('div', { class: 'faq-item' }, [
      renderElement('h3', { class: 'faq-question' }, item.question),
      renderElement('div', { class: 'faq-answer' }, item.answer)
    ]);
    container.appendChild(itemDiv);
  });
}

/* ==============================
   7. FAQ Accordion Interaction
   ============================== */
function initFAQAccordion() {
  const items = document.querySelectorAll('.faq-item');
  items.forEach(item => {
    const question = item.querySelector('.faq-question');
    question.addEventListener('click', () => {
      const answer = item.querySelector('.faq-answer');
      answer.style.display = answer.style.display === 'block' ? 'none' : 'block';
      item.classList.toggle('active');
    });
  });
}

/* ==============================
   8. Initialize All Pages
   ============================== */
document.addEventListener('DOMContentLoaded', () => {
  try {
  // Initialize IntersectionObserver for reveal animations
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('active');
        if (entry.target.dataset.reveal === 'true') observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.05, rootMargin: '0px 0px 50px 0px' });

  // Observe all reveal elements
  const observeReveals = () => {
    document.querySelectorAll('.reveal:not([data-reveal]), .animate-on-scroll:not([data-reveal])').forEach(el => {
      el.dataset.reveal = 'true';
      observer.observe(el);
    });
  };
  observeReveals();

  // FAQ accordion
  initFAQAccordion();

  // If on catalog page
  if (document.getElementById('catalogGrid')) {
    populateProductCatalog();
  }

  // If on blog listing page (homepage preview or full blog page)
  if (document.getElementById('blogPreviewGrid') || document.getElementById('blogListingGrid')) {
    populateBlogListing();
  }

  // If on blog post page
  if (document.getElementById('postContent')) {
    populateBlogPost();
  }

  // If on about page
  if (document.getElementById('teamGrid')) {
    populateTeam();
  }

  // If on FAQ page (if exists)
  if (document.getElementById('faqContainer')) {
    populateFAQ();
  }

  // If on product detail page
  const productId = new URLSearchParams(window.location.search).get('id') || (document.getElementById('productTitle') ? '1' : null);
  if (productId && document.getElementById('mainImage')) {
    populateProductDetail(productId);
  }

  // Observe dynamically created reveal elements (product cards, blog cards, etc.)
  observeReveals();
  } catch (e) {
    console.error('Init error:', e);
  }
});

/* ======================================
   9. Product Detail Page Logic
   ====================================== */
function populateProductDetail(id) {
  const product = products.find(p => p.id == id);
  if (!product) return;

  // Title & breadcrumb
  const titleEl = document.getElementById('productTitle');
  if (titleEl) titleEl.textContent = product.name;
  const breadcrumbEl = document.getElementById('productBreadcrumbTitle');
  if (breadcrumbEl) breadcrumbEl.textContent = product.name;
  document.title = `${product.name} | مبلمان میراکل`;

  // Category
  const categoryEl = document.getElementById('productCategory');
  if (categoryEl) categoryEl.textContent = product.category;

  // Description
  const descEl = document.getElementById('productDescription');
  if (descEl) descEl.textContent = product.description;

  // Main image
  const mainImg = document.getElementById('mainImage');
  if (mainImg) {
    mainImg.src = product.images[0];
    mainImg.alt = product.name;
  }

  // Thumbnails
  const container = document.getElementById('thumbnailsContainer');
  if (container) {
    container.innerHTML = '';
    product.images.slice(1).forEach(src => {
      const thumb = renderElement('img', { src, alt: product.name, class: 'product-thumbnail' });
      container.appendChild(thumb);
    });
  }

  // Specs
  const specsGrid = document.getElementById('specsGrid');
  if (specsGrid) {
    specsGrid.innerHTML = '';
    Object.entries(product.specs).forEach(([key, value]) => {
      const specItem = renderElement('div', { class: 'spec-item' }, [
        renderElement('div', { class: 'spec-label' }, key),
        renderElement('div', { class: 'spec-value' }, value)
      ]);
      specsGrid.appendChild(specItem);
    });
  }

  // Price
  const priceEl = document.getElementById('productPrice');
  if (priceEl) priceEl.textContent = product.price > 0 ? formatPrice(product.price) : 'استعلام قیمت';

  // Initialize interactive fabric color customizer
  initProductColorCustomizer(product);
}

/* ======================================
   10. Interactive Fabric Color Customizer
   ====================================== */
const fabricColors = [
  { id: 'original', name: 'کرم شیری (رنگ اصلی کاتالوگ)', color: null, hex: '#FAF3E8', isLight: true },
  { id: 'light-brown', name: 'قهوه‌ای روشن (شنی / کاراملی)', color: '#C89B6D', hex: '#C89B6D' },
  { id: 'mocha', name: 'نسکافه‌ای / قهوه‌ای موکا', color: '#9E7453', hex: '#9E7453' },
  { id: 'chocolate', name: 'شکلاتی گرم / بلوطی', color: '#63402A', hex: '#63402A' },
  { id: 'soft-grey', name: 'طوسی فیلی مینیمال', color: '#9FA3A8', hex: '#9FA3A8', isLight: true },
  { id: 'dark-grey', name: 'طوسی ذغالی شیک', color: '#53565C', hex: '#53565C' },
  { id: 'emerald', name: 'سبز یشمی سلطنتی', color: '#3D5948', hex: '#3D5948' },
  { id: 'navy', name: 'سرمه‌ای عمیق کلاسیک', color: '#293B52', hex: '#293B52' },
  { id: 'mustard', name: 'خردلی کتان گرم', color: '#CCA046', hex: '#CCA046' },
  { id: 'burgundy', name: 'زرشکی مخملی', color: '#7A2D35', hex: '#7A2D35' }
];

function initProductColorCustomizer(product) {
  const customizer = document.getElementById('colorCustomizer');
  const overlay = document.getElementById('fabricOverlay');
  const liveBadge = document.getElementById('fabricLiveBadge');
  const liveText = document.getElementById('fabricLiveText');
  const activeColorName = document.getElementById('activeColorName');
  const swatchesGrid = document.getElementById('colorSwatchesGrid');
  const resetBtn = document.getElementById('resetColorBtn');

  if (!customizer || !overlay || !swatchesGrid) return;

  // Retrieve base64 RGBA mask (bypasses file:/// CORS and supports native alpha masking)
  const masks = (typeof sofaMasks !== 'undefined' && sofaMasks) ? sofaMasks : (window.sofaMasks || {});
  const maskData = masks[product.id];

  // Only sofas have fabric recoloring (tables do not)
  if (!maskData) {
    customizer.style.display = 'none';
    overlay.style.display = 'none';
    if (liveBadge) liveBadge.style.display = 'none';
    return;
  }

  // Reset any previous overlay state
  overlay.classList.remove('active');
  overlay.style.backgroundColor = 'transparent';
  if (liveBadge) liveBadge.style.display = 'none';

  // Set base64 mask for this product with full browser prefixes
  customizer.style.display = 'block';
  overlay.style.display = 'block';
  overlay.style.webkitMaskImage = `url("${maskData}")`;
  overlay.style.maskImage = `url("${maskData}")`;
  overlay.style.webkitMaskSize = '100% 100%';
  overlay.style.maskSize = '100% 100%';
  overlay.style.webkitMaskPosition = 'center';
  overlay.style.maskPosition = 'center';
  overlay.style.webkitMaskRepeat = 'no-repeat';
  overlay.style.maskRepeat = 'no-repeat';

  // Render Swatches
  swatchesGrid.innerHTML = '';
  let activeSwatchEl = null;

  function applyColor(colorVal, name, swatchEl) {
    if (activeSwatchEl) activeSwatchEl.classList.remove('active');
    if (swatchEl) {
      swatchEl.classList.add('active');
      activeSwatchEl = swatchEl;
    }

    if (activeColorName) activeColorName.textContent = name;

    if (!colorVal) {
      // Original color
      overlay.classList.remove('active');
      overlay.style.backgroundColor = 'transparent';
      if (liveBadge) liveBadge.style.display = 'none';
      updateConsultLinks(product.name, null);
    } else {
      overlay.style.backgroundColor = colorVal;
      overlay.classList.add('active');
      if (liveBadge) {
        liveBadge.style.display = 'inline-flex';
        if (liveText) liveText.textContent = `پیش‌نمایش: ${name}`;
      }
      updateConsultLinks(product.name, name);
    }
  }

  function updateConsultLinks(productName, colorName) {
    const consultBtn = document.getElementById('consultBtn') || document.querySelector('.product-actions a.btn-primary');
    if (consultBtn) {
      if (colorName) {
        const msg = `سلام، قصد استعلام قیمت و مشاوره برای ${productName} با رنگ پارچه «${colorName}» را دارم.`;
        consultBtn.href = `https://wa.me/989132160811?text=${encodeURIComponent(msg)}`;
        consultBtn.innerHTML = `
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>استعلام قیمت (${colorName})</span>`;
      } else {
        const msg = `سلام، قصد استعلام قیمت و مشاوره برای ${productName} را دارم.`;
        consultBtn.href = `https://wa.me/989132160811?text=${encodeURIComponent(msg)}`;
        consultBtn.innerHTML = `
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>درخواست خرید و مشاوره</span>`;
      }
    }

    // Also dynamically update the "پارچه" row in specs
    const specItems = document.querySelectorAll('#specsGrid .spec-item');
    specItems.forEach(item => {
      const label = item.querySelector('.spec-label');
      const val = item.querySelector('.spec-value');
      if (label && label.textContent.includes('پارچه') && val) {
        val.textContent = colorName ? `رنگ انتخابی: ${colorName}` : (product.specs['پارچه'] || 'دلخواه به سلیقه مشتری');
      }
    });
  }

  // Initial consult link setup
  updateConsultLinks(product.name, null);

  // Create preset swatches
  fabricColors.forEach((item, index) => {
    const swatch = renderElement('button', {
      type: 'button',
      class: `color-swatch-item ${item.isLight ? 'light' : ''} ${index === 0 ? 'active' : ''}`,
      style: `background-color: ${item.hex};`,
      'aria-label': item.name
    }, [
      renderElement('span', { class: 'color-swatch-tooltip' }, item.name)
    ]);

    if (index === 0) activeSwatchEl = swatch;

    swatch.addEventListener('click', () => {
      applyColor(item.color, item.name, swatch);
    });

    swatchesGrid.appendChild(swatch);
  });

  // Add custom color picker swatch
  const customSwatch = renderElement('div', {
    class: 'color-swatch-item color-swatch-custom',
    title: 'انتخاب رنگ دلخواه'
  }, [
    renderElement('span', { class: 'color-swatch-tooltip' }, 'پالت رنگ دلخواه'),
    renderElement('input', {
      type: 'color',
      value: '#C89B6D',
      'aria-label': 'انتخاب رنگ سفارشی پارچه'
    })
  ]);

  const colorInput = customSwatch.querySelector('input');
  colorInput.addEventListener('input', (e) => {
    const hex = e.target.value;
    applyColor(hex, `رنگ سفارشی (${hex})`, customSwatch);
  });
  customSwatch.addEventListener('click', (e) => {
    if (e.target !== colorInput) {
      colorInput.click();
    }
  });

  swatchesGrid.appendChild(customSwatch);

  // Reset button
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      const firstSwatch = swatchesGrid.querySelector('.color-swatch-item');
      applyColor(null, fabricColors[0].name, firstSwatch);
    });
  }
}

/* ==============================
   10. Smooth Scroll (already present)
   ====================================== */