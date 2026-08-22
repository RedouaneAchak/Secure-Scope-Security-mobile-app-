package pfa.redouaneachak.securescope.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pfa.redouaneachak.securescope.data.model.RiskScore

@Composable
fun RiskBadge(riskScore: RiskScore, modifier: Modifier = Modifier) {
    val (color, label) = when (riskScore) {
        RiskScore.LOW -> Color(0xFF22C55E) to "Low"
        RiskScore.MEDIUM -> Color(0xFFF59E0B) to "Medium"
        RiskScore.HIGH -> Color(0xFFF97316) to "High"
        RiskScore.CRITICAL -> Color(0xFFEF4444) to "Critical"
    }

    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}