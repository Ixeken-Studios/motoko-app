package com.ixeken.motoko.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.motoko.R
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private val PageDark: Color
    @Composable
    get() = LocalMotokoColors.current.primaryDark

private val PageWhite: Color
    @Composable
    get() = LocalMotokoColors.current.surfaceCard

data class OnboardingPage(
    val titleRes: Int,
    val descRes: Int,
    val accentColor: Color
)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_page1_title, R.string.onboarding_page1_desc, Color(0xFF4A90D9)),
    OnboardingPage(R.string.onboarding_page2_title, R.string.onboarding_page2_desc, Color(0xFF50B86C)),
    OnboardingPage(R.string.onboarding_page3_title, R.string.onboarding_page3_desc, Color(0xFFE8913A)),
    OnboardingPage(R.string.onboarding_page4_title, R.string.onboarding_page4_desc, Color(0xFF9B59B6))
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(page = page, pagerState = pagerState)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) LocalMotokoColors.current.primaryDark
                                else LocalMotokoColors.current.textMuted.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isLastPage = pagerState.currentPage == pages.size - 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isLastPage) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.primaryDark)
                    .clickable {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLastPage) stringResource(id = R.string.onboarding_btn_start)
                    else stringResource(id = R.string.onboarding_btn_next),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textOnDark
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    pagerState: PagerState
) {
    val pageOffset = {
        val diff = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        diff.absoluteValue
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = (1f - pageOffset().coerceIn(0f, 1f) * 0.3f)
            }
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(pages[page].accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            PageIllustration(page = page)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = pages[page].titleRes),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = pages[page].descRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = LocalMotokoColors.current.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PageIllustration(page: Int) {
    when (page) {
        0 -> TransactionIllustration()
        1 -> WalletIllustration()
        2 -> SubscriptionIllustration()
        3 -> PrivacyIllustration()
    }
}

@Composable
private fun TransactionIllustration() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PageWhite.copy(alpha = 0.9f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PageDark)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(if (index % 2 == 0) 80.dp else 60.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageDark.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF6C757D).copy(alpha = 0.4f))
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index == 0) Color(0xFF50B86C).copy(alpha = 0.6f)
                            else Color(0xFFE74C3C).copy(alpha = 0.6f)
                        )
                )
            }
        }
    }
}

@Composable
private fun WalletIllustration() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PageWhite.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4A90D9).copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageDark.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF6C757D).copy(alpha = 0.4f))
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PageWhite.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF50B86C).copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageDark.copy(alpha = 0.2f))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PageWhite.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8913A).copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageDark.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionIllustration() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PageWhite.copy(alpha = 0.9f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PageDark)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageDark.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF6C757D).copy(alpha = 0.4f))
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(50.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8913A).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PageWhite.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyIllustration() {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PageWhite.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF50B86C).copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PageWhite)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PageWhite.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF4A90D9).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF4A90D9).copy(alpha = 0.6f))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PageDark.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF50B86C).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF50B86C).copy(alpha = 0.6f))
            )
        }
    }
}
