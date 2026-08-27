/**
 * 生成Markdown格式的批改报告
 */
export function generateMarkdownReport(result, topic, userEssay, essayType) {
  const now = new Date()
  const dateStr = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })

  const essayTypeLabels = {
    'EN1_PICTURE': '英语一图画作文',
    'EN2_CHART': '英语二图表作文',
    'LETTER': '应用文/小作文'
  }

  let markdown = `# 考研英语作文批改报告

**生成时间：** ${dateStr}
**作文类型：** ${essayTypeLabels[essayType] || essayType}

---

## 作文题目

${topic || '无'}

## 学生原文

\`\`\`
${userEssay}
\`\`\`

---

## 批改结果

### 总分

**${result.total_score}** / ${result.total_score > 10 ? '20' : '10'}

`

  // 分项得分
  if (result.breakdown) {
    const labels = {
      content: '内容完整性',
      language: '语言准确性',
      vocabulary: '词汇多样性',
      structure: '句式丰富度',
      format: '格式规范',
      appropriacy: '交际得体',
      completeness: '信息完整'
    }

    markdown += `### 分项得分

| 评分项 | 得分 |
|--------|------|
`

    for (const [key, value] of Object.entries(result.breakdown)) {
      markdown += `| ${labels[key] || key} | ${value} / 5 |\n`
    }

    markdown += '\n'
  }

  // 错误标注
  if (result.errors && result.errors.length > 0) {
    markdown += `### 错误标注（共 ${result.errors.length} 处）

| 序号 | 原文 | 修改后 | 错误原因 | 类型 |
|------|------|--------|----------|------|
`

    result.errors.forEach((error, index) => {
      markdown += `| ${index + 1} | ${error.original} | ${error.corrected} | ${error.reason} | ${error.type} |\n`
    })

    markdown += '\n'
  }

  // 整体建议
  if (result.weaknesses) {
    markdown += `### 整体不足与修改建议

${result.weaknesses}

`
  }

  // 润色全文
  if (result.polished_essay) {
    markdown += `### 润色优化后的全文

\`\`\`
${result.polished_essay}
\`\`\`

`
  }

  // 高级表达
  if (result.advanced_phrases && result.advanced_phrases.length > 0) {
    markdown += `### 可复用高级表达

${result.advanced_phrases.map(phrase => `- ${phrase}`).join('\n')}

`
  }

  // 降级提示
  if (result.degraded) {
    markdown += `---

**⚠️ 注意：** 本次为降级结果，格式解析失败，仅供参考。

`
  }

  markdown += `---

*报告由考研英语作文批改Agent生成*`

  return markdown
}

/**
 * 下载Markdown报告
 */
export function downloadMarkdownReport(markdown, filename = '作文批改报告.md') {
  const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 生成带时间戳的文件名
 */
export function generateReportFilename() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')

  return `作文批改报告_${year}${month}${day}_${hours}${minutes}${seconds}.md`
}