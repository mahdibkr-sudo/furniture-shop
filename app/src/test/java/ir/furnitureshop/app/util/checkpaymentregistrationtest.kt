package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست مسیر «ثبت پرداختی با روش چک از پروفایل مشتری».
 *
 * کل زنجیره را بدون Android شبیه‌سازی می‌کند:
 * ورودی فارسی مبلغ ← نرمال‌سازی ← Payment + Check (اتمیک) ← تراز مشتری ← نمایش در سال جاری.
 *
 * هدف: اطمینان از اینکه منطق ثبت چک (قبل از لایه Room/UI) در هیچ حالتی
 * ورودی نامعتبر تولید نمی‌کند و تراز مالی و قوانین (Double Counting ممنوع)
 * دست‌نخورده می‌مانند.
 */
class CheckPaymentRegistrationTest {

    /** سناریوی واقعی: مبلغ با ارقام فارسی + جداکننده، شماره چک با فاصله، بدون سررسید */
    @Test
    fun `full check registration from customer profile`() {
        val customerId = 7L

        // کاربر در «ثبت پرداختی» مبلغ را با ارقام فارسی و جداکننده وارد کرده است
        val amountText = "۲٬۵۰۰٬۰۰۰"
        val amount = DigitNormalizer.digitsOnly(amountText).toLongOrNull() ?: 0L
        assertEquals(2_500_000L, amount)
        assertTrue("مبلغ باید مثبت باشد", amount > 0)

        // شماره چک با فاصله‌های اضافی وارد شده است
        val checkNumber = " 1234567 "
        val (payment, check) = CheckFlow.receiveFromCustomer(
            customerId = customerId,
            amount = amount,
            checkNumber = checkNumber,
            bank = "ملت",
            dueDate = null
        )

        // Payment روش چک دارد و به مشتری متصل است
        assertEquals(customerId, payment.customerId)
        assertEquals(amount, payment.amount)
        assertEquals(PaymentMethod.CHECK.stored, payment.method)

        // Check درست ساخته شده: شماره trim شده، در دست فروشگاه
        assertEquals("1234567", check.checkNumber)
        assertEquals(customerId, check.receivedFromCustomerId)
        assertEquals(CheckStatus.IN_HAND.stored, check.status)
        assertEquals(CheckFlow.HOLDER_SHOP, check.currentHolder)
        assertTrue(CheckFlow.canTransfer(check))
        assertEquals("ملت", check.bank)

        // چک و پرداخت امروز ثبت شده‌اند؛ در «سال مالی جاری» دیده می‌شوند.
        // (اگر این تست شکست بخورد یعنی YearFilter چک تازه‌ثبت‌شده را پنهان می‌کند)
        assertTrue(
            "چک تازه‌ثبت‌شده باید در سال جاری شمسی دیده شود",
            YearFilter.isInYear(check.receiveDate, YearFilter.currentJalaliYear())
        )
        assertTrue(
            "پرداخت تازه‌ثبت‌شده باید در سال جاری شمسی دیده شود",
            YearFilter.isInYear(payment.date, YearFilter.currentJalaliYear())
        )
    }

    /** تراز مشتری بعد از ثبت چک: فاکتور − Payment(چک) = بدهی باقی‌مانده */
    @Test
    fun `balance after check payment`() {
        val invoice = Invoice(
            id = 1,
            customerId = 1,
            invoiceNumber = "INV-0001",
            totalAmount = 5_000_000
        )
        val (payment, _) = CheckFlow.receiveFromCustomer(
            customerId = 1,
            amount = 2_000_000,
            checkNumber = "CHK-777",
            bank = null,
            dueDate = null
        )

        val balance = BalanceCalculator.balance(listOf(invoice), listOf(payment))
        assertEquals(3_000_000L, balance)

        // چک به تنهایی در تراز ظاهر نمی‌شود — فقط Payment اثر دارد (قانون Double Counting)
        val balanceWithoutPayment = BalanceCalculator.balance(listOf(invoice), emptyList())
        assertEquals(5_000_000L, balanceWithoutPayment)
    }

    /** شماره چک تکراری با ارقام فارسی/عربی vs انگلیسی باید شناسایی شود */
    @Test
    fun `duplicate check number detected across persian and english digits`() {
        val existing = listOf("۱۲۳۴", "CHK-001", "chk-002")

        // فارسی → انگلیسی (همان شماره)
        assertTrue(CheckFlow.isDuplicateNumber(existing, "1234"))
        // انگلیسی → فارسی
        assertTrue(CheckFlow.isDuplicateNumber(existing, "۱۲۳۴"))
        // نادیده‌گرفتن حروف بزرگ
        assertTrue(CheckFlow.isDuplicateNumber(existing, "chk-001"))
        // شماره کاملاً جدید
        assertFalse(CheckFlow.isDuplicateNumber(existing, "CHK-999"))
    }

    /** سررسید انتخاب‌شده با تقویم شمسی باید round-trip دقیق داشته باشد */
    @Test
    fun `due date jalali round trip`() {
        val millis = JalaliDate.toEpochMillis(1405, 6, 1)
        val (jy, jm, jd) = JalaliDate.fromEpochMillis(millis)
        assertEquals(1405, jy)
        assertEquals(6, jm)
        assertEquals(1, jd)
        assertEquals("۱۴۰۵/۰۶/۰۱", JalaliDate.formatDate(millis))
    }

    /** چک دریافتی بدون سررسید (اختیاری) نباید مشکلی در نمایش ایجاد کند */
    @Test
    fun `check without due date renders safely`() {
        val (_, check) = CheckFlow.receiveFromCustomer(
            customerId = 3,
            amount = 100_000,
            checkNumber = "NO-DUE-DATE",
            bank = null,
            dueDate = null
        )
        // دقیقاً همان مسیری که UI برای نمایش «سررسید» طی می‌کند
        val dueLabel = check.dueDate?.let { JalaliDate.formatDate(it) } ?: "—"
        assertEquals("—", dueLabel)
    }

    /** ثبت نقدی (بدون چک) مسیر مستقل خود را دارد و وارد جدول چک نمی‌شود */
    @Test
    fun `cash payment stays independent of check flow`() {
        // روش نقد: فقط Payment — هیچ Check ساخته نمی‌شود
        val payment = ir.furnitureshop.app.data.entity.Payment(
            customerId = 2,
            amount = 1_000_000,
            date = System.currentTimeMillis(),
            method = PaymentMethod.CASH.stored
        )
        assertEquals(PaymentMethod.CASH.stored, payment.method)

        // تراز با Payment نقدی
        val invoice = Invoice(id = 2, customerId = 2, invoiceNumber = "INV-0002", totalAmount = 1_500_000)
        assertEquals(500_000L, BalanceCalculator.balance(listOf(invoice), listOf(payment)))
    }
}