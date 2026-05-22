package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberpunkYellow
import com.example.ui.theme.DeepSpaceBlack

@Composable
fun VeloRixButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = CyberpunkYellow,
    textColor: Color = DeepSpaceBlack,
    glowColor: Color = CyberpunkYellow,
    shape: RoundedCornerShape = RoundedCornerShape(50.dp), // Stadium-shape (Pill)
    testTag: String = "velorix_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring scale animation on button press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "velo_btn_scale"
    )

    // Compute glow depth
    val shadowElevation = if (isPressed) 4.dp else 10.dp

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                clip = false,
                ambientColor = glowColor.copy(alpha = 0.4f),
                spotColor = glowColor
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor,
            contentColor = textColor,
            disabledContainerColor = accentColor.copy(alpha = 0.3f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = text.toUpperCase(Locale.current),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.25.sp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
