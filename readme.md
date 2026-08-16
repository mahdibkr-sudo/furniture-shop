# فروشگاه مبل و میز — اپلیکیشن مدیریت کسب‌وکار

اپلیکیشن Android برای مدیریت یک کسب‌وکار فروش مبل و میز: کاملاً آفلاین، تک‌دستگاهی، بدون حساب کاربری و بدون سرور.

## وضعیت فعلی (Task FINAL — AUDIT کامل + UI Polish + رفع کرش)

- ✅ ساختار پروژه (MVVM + Repository + Room)
- ✅ Room Database نسخه ۹ با ۱۱ Entity — Migration های صریح ۱→۹ (بدون fallbackToDestructiveMigration)
- ✅ سیستم محصولات (CRUD + جستجو + عکس + قیمت پایه)
- ✅ سیستم مشتریان (لیست/جستجو/حذف امن + پروفایل + تراز)
- ✅ سیستم فاکتور (قیمت تاریخی + تخفیف + پارچه با متراژ و قیمت هر متر)
- ✅ پرداخت مشتری (نقد/چک + تراز بدون Double Counting)
- ✅ سیستم تأمین‌کنندگان (لیست/جستجو/حذف امن + پروفایل)
- ✅ خرید جنسی از تأمین‌کننده + پرداخت نقد + تراز Supplier
- ✅ سیستم یکپارچه Check (دریافت از مشتری → انتقال اتمیک همان چک به تأمین‌کننده، شماره یکتا)
- ✅ پنل Expense (هزینه‌های جانبی مستقل از Supplier)
- ✅ پنل «حساب مالی» — وضعیت مالی کلی از داده‌های موجود
- ✅ صفحه اصلی — Dashboard سبک و فقط ناوبری به ۷ بخش
- ✅ آیکون اپ — مبل/کاناپه مینیمال (سفارشی، جایگزین پیش‌فرض)
- ✅ جداکننده هزارگان (٬) روی همه اعداد + ارقام فارسی — `PriceFormatter`
- ✅ تاریخ شمسی (جلالی) در همه نمایش‌ها + **تقویم انتخاب تاریخ شمسی** — `JalaliDate` (الگوریتم ۳۳ ساله، بدون کتابخانه خارجی)
- ✅ پارچه در فاکتور: **متراژ × قیمت هر متر** (Migration ۸→۹؛ فاکتورهای قدیمی فقط مبلغ دارند)
- ✅ خروجی فاکتور PDF / Image — فارسی، RTL، اشتراک‌گذاری با FileProvider
- ✅ طراحی فاکتور: مینیمال با نام فروشگاه «میراکل»، حاشیه دور فاکتور، سربرگ در کادر، جدول اقلام دوستونه، باکس مبلغ نهایی
- ✅ AUDIT نهایی: گارد Double-Submit در همه فرم‌ها (جلوگیری از Duplicate)، پرداخت چک Supplier فقط از مسیر انتقال همان چک
- ✅ **رفع کرش ناگهانی**: گارد `catch` روی همه ViewModel ها (خطا به‌جای کرش، در UI نمایش داده می‌شود)، try/catch در همه ذخیره/حذف‌ها، دیکد هوشمند تصاویر (ضد OOM)
- ✅ **CrashLogger**: هر کرش در فایل محلی ثبت و در صفحه اصلی با دکمه «کپی متن خطا» نمایش داده می‌شود — دیگر هیچ کرشی بی‌صدا نمی‌ماند
- ✅ فیلدهای ورودی عددی بدون جداکننده زنده (رفع پرش cursor و ناپدید شدن صفر)؛ جداکننده فقط در نمایش‌های فقط‌خواندنی

## تکنولوژی‌ها

| لایه | انتخاب |
|---|---|
| زبان | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 (BOM 2024.12.01) |
| معماری | MVVM + Repository |
| دیتابیس | Room 2.6.1 (kapt) |
| Async | Coroutines + Flow |
| ناوبری | Navigation Compose 2.8.5 |
| Min/Target SDK | 24 / 35 |

## ساختار پروژه

```
app/src/main/java/ir/furnitureshop/app/
├─ FurnitureApplication.kt      ← DI دستی + نصب CrashLogger
├─ MainActivity.kt              ← RTL + Theme + NavHost
├─ data/
│  ├─ local/AppDatabase.kt      ← Room Database (نسخه ۹ + Migration ۱→۹)
│  ├─ local/ProductImageStore.kt← ذخیره تصویر با کوچک‌سازی خودکار (۱۰۲۴px)
│  ├─ dao/                      ← ۹ DAO (Product..Expense)
│  ├─ entity/                   ← ۱۱ Entity + PaymentMethod/CheckStatus/CheckFlow
│  └─ repository/               ← ۸ Repository
├─ ui/
│  ├─ screens/                  ← Home + محصولات + مشتریان + فاکتور + چک + Expense + حساب مالی
│  ├─ components/               ← ProductImage (دیکد هوشمند), JalaliDatePickerDialog
│  └─ theme/Color.kt, Theme.kt, Type.kt
├─ navigation/AppNavHost.kt     ← Routes (همه صفحه‌ها)
├─ util/                        ← PriceFormatter, Calculators, DigitNormalizer, JalaliDate, CrashLogger, InvoicePrint/InvoiceExport
└─ viewmodel/                   ← ۱۹ ViewModel
```

## Entityها (نسخه ۹ دیتابیس)

`Product` · `Customer` · `Invoice` · `InvoiceItem` · `Payment` · `Supplier` · `Purchase` · `Check` · `Expense` · `SupplierPayment`

> `Invoice` شامل `fabricMeters` (متراژ پارچه) و `fabricUnitPrice` (قیمت هر متر) است؛ `fabricAmount` = متراژ × قیمت واحد (برای فاکتورهای قدیمی فقط مبلغ موجود است).

## قوانین مالی (تست‌شده)

- **Customer Balance** = Σ فاکتورها − Σ پرداخت‌ها (نقد و چک) — `BalanceCalculator`
- **Supplier Balance** = Σ خرید جنسی − Σ پرداخت‌ها (نقد و چک) — `SupplierBalanceCalculator`
- **چک به تنهایی مبلغ مالی جدید ایجاد نمی‌کند**؛ Payment روی Balance اثر دارد؛ یک چک دوبار محاسبه نمی‌شود
- **Expense مستقل از Supplier** است و تراز تأمین‌کننده را تغییر نمی‌دهد؛ فقط در «حساب مالی» دیده می‌شود
- **حساب مالی**: نتیجه = فروش − خرید جنسی − هزینه‌های جانبی + تراز مشتریان/تأمین‌کنندگان — `FinancialOverviewCalculator`

## قوانین نمایش (تست‌شده)

- **اعداد**: ارقام فارسی با جداکننده هزارگان «٬» (مثلاً «۲٬۰۰۰٬۰۰۰») در همه نمایش‌ها؛ متراژ با ممیز فارسی «٫» (مثلاً «۳٫۵ متر»)
- **شناسه‌ها** (شماره چک، شماره فاکتور، #id) عمداً بدون جداکننده — شناسه‌اند نه کمیت
- **تاریخ**: همه تاریخ‌ها شمسی با ارقام فارسی (مثلاً «۱۴۰۵/۰۵/۲۵»)؛ انتخاب تاریخ با تقویم شمسی؛ ذخیره‌سازی همچنان epoch millis

## پایداری (ضد کرش)

- همه ViewModel های دارای Flow مجهز به `.catch` هستند — خطای دیتابیس به‌جای کرش، در UI نمایش داده می‌شود
- همه ذخیره/حذف/بارگذاری‌ها try/catch دارند
- تصاویر با خواندن ابعاد + downsampling هوشمند دیکد می‌شوند (ضد OOM)
- `CrashLogger` stacktrace هر کرش را در `crash_log.txt` ذخیره می‌کند و در صفحه اصلی با دکمه «کپی متن خطا» نمایش می‌دهد

## اجرا

### با Android Studio (پیشنهادی)
1. Android Studio را باز کنید → `Open` → پوشه `furniture-shop` را انتخاب کنید.
2. اجازه دهید Sync انجام شود، سپس `Run ▶` روی یک دستگاه/امولاتور.

### خط فرمان
```
gradlew.bat testDebugUnitTest assembleDebug
```
خروجی: `app/build/outputs/apk/debug/app-debug.apk`

پیش‌نیاز: JDK 17+ و Android SDK (Platform 35). روی این سیستم: JDK در `C:\Users\Mahdi\Android\jdk-17` و SDK در `C:\Users\Mahdi\Android\sdk` نصب شد.

## نکات

- **بدون Backup**: `allowBackup="false"` در Manifest — مطابق قانون پروژه.
- **بدون اینترنت**: هیچ permission شبکه‌ای تعریف نشده.
- **آیکون**: مبل/کاناپه مینیمال (کرم روی پس‌زمینه چوبی) — آیکون تطبیقی + نسخه‌های قدیمی (minSdk 24).
- **فونت**: فونت پیش‌فرض سیستم؛ برای ظاهر بهتر فارسی می‌توان وزیرمتن اضافه کرد.
- **RTL**: در `MainActivity` با `LayoutDirection.Rtl` اجباری شده — مستقل از زبان دستگاه.
- **تست‌ها**: ۹۴ تست واحد در `app/src/test/java/ir/furnitureshop/app/util/` (PriceFormatter, InvoiceCalculator, BalanceCalculator, SupplierBalanceCalculator, CheckFlow, ExpenseAndOverview, FinancialAccount, Routes, InvoicePrint, EndToEndScenario, FinalFinancialAudit, JalaliDate, CustomerProfileState).

## مسیر بعدی

گزارش‌گیری/خروجی، آمار، تنظیمات یا هر تسک جدیدی که کاربر مشخص کند.
