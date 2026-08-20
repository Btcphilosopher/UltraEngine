package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UltraEngineViewModel
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishAmberContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishGreenContainer
import com.example.ui.theme.PolishGreenDark
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishRedContainer
import com.example.ui.theme.PolishRedDark
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.examples.MarketQuote
import com.example.ultraengine.examples.Side
import com.example.ultraengine.examples.TradeMatch

@Composable
fun MarketDataScreen(
    viewModel: UltraEngineViewModel,
    modifier: Modifier = Modifier
) {
    val quotes by viewModel.marketQuotes.collectAsState()
    val matches by viewModel.orderBookMatches.collectAsState()
    val readings by viewModel.sensorReadings.collectAsState()

    var orderPriceText by remember { mutableStateOf("64250.00") }
    var orderQtyText by remember { mutableStateOf("50") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PolishCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PolishSurfaceHeader)
                    .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = PolishPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Domain Engines",
                                color = PolishTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "L2 FEED • MATCHING • SENSORS",
                                color = PolishPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Limit Order Submission Form ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurface)
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORDER BOOK MATCHING (BTC-USD)",
                            color = PolishTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = PolishPurple, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = orderPriceText,
                            onValueChange = { orderPriceText = it },
                            label = { Text("Price", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolishPurple,
                                unfocusedBorderColor = PolishBorder,
                                focusedTextColor = PolishTextPrimary,
                                unfocusedTextColor = PolishTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = orderQtyText,
                            onValueChange = { orderQtyText = it },
                            label = { Text("Quantity", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolishPurple,
                                unfocusedBorderColor = PolishBorder,
                                focusedTextColor = PolishTextPrimary,
                                unfocusedTextColor = PolishTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val price = orderPriceText.toDoubleOrNull() ?: 64200.0
                                val qty = orderQtyText.toLongOrNull() ?: 10L
                                viewModel.submitManualOrder(Side.BUY, price, qty)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishGreen, contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "BUY LIMIT", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val price = orderPriceText.toDoubleOrNull() ?: 64200.0
                                val qty = orderQtyText.toLongOrNull() ?: 10L
                                viewModel.submitManualOrder(Side.SELL, price, qty)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishRed, contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "SELL LIMIT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Recent Trade Matches Feed ---
        if (matches.isNotEmpty()) {
            item {
                Text(
                    text = "MATCHED TRADES (FIFO / MICROSECOND LATENCY)",
                    color = PolishTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            items(matches.take(4)) { trade ->
                TradeMatchRow(match = trade)
            }
        }

        // --- Live Level 2 Market Quote Stream ---
        item {
            Text(
                text = "LIVE MARKET DATA FEED (L2 QUOTES)",
                color = PolishTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        items(quotes.take(6)) { quote ->
            QuoteRow(quote = quote)
        }

        // --- Industrial Telemetry Sensor Pipeline ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENSOR TELEMETRY & ANOMALY INGEST",
                    color = PolishTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = PolishAmber, modifier = Modifier.size(16.dp))
            }
        }

        items(readings.take(4)) { sensor ->
            SensorReadingRow(sensor = sensor)
        }
    }
}

@Composable
fun TradeMatchRow(match: TradeMatch) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PolishSurface)
            .border(1.dp, PolishGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MATCH #${match.matchId} (Qty: ${match.quantity})",
                    color = PolishGreenDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Buy #${match.buyOrderId} ↔ Sell #${match.sellOrderId}",
                    color = PolishTextMuted,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("$%.2f", match.price),
                    color = PolishTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("Exec Latency: %.2f μs", match.latencyNanos / 1000.0),
                    color = PolishPurple,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun QuoteRow(quote: MarketQuote) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PolishSurface)
            .border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = quote.symbol,
                    color = PolishTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("lat: %.1fμs", quote.latencyMicros),
                    color = PolishPurple,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "BID", color = PolishTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.2f", quote.bidPrice),
                        color = PolishGreenDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ASK", color = PolishTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.2f", quote.askPrice),
                        color = PolishRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun SensorReadingRow(sensor: com.example.ultraengine.examples.SensorReading) {
    val borderColor = if (sensor.isAnomaly) PolishRed else PolishBorder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (sensor.isAnomaly) PolishRedContainer.copy(alpha = 0.5f) else PolishSurface)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (sensor.isAnomaly) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = PolishRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "Sensor #${sensor.sensorId}",
                    color = if (sensor.isAnomaly) PolishRedDark else PolishTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "${String.format("%.1f", sensor.temperatureC)}°C", color = PolishTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                Text(text = "${String.format("%.1f", sensor.vibrationHz)}Hz", color = PolishTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                Text(text = "${String.format("%.1f", sensor.pressurePsi)}psi", color = PolishTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
            }
        }
    }
}

