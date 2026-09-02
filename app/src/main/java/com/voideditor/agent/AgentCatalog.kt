package com.voideditor.agent

import androidx.annotation.DrawableRes
import com.voideditor.R

data class AgentSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val binary: String,
    val docUrl: String,
    @DrawableRes val iconRes: Int,
    val installScript: String
)

object AgentCatalog {

    private const val EnsureNode = """
if ! command -v node >/dev/null 2>&1; then
  echo "==> installing Node.js runtime"
  apt-get update -y
  apt-get install -y curl ca-certificates gnupg
  curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
  apt-get install -y nodejs
fi
echo "==> node $(node --version), npm $(npm --version)"
"""

    private fun npmAgent(
        id: String,
        name: String,
        subtitle: String,
        binary: String,
        docUrl: String,
        @DrawableRes iconRes: Int,
        pkg: String
    ) = AgentSpec(
        id = id,
        name = name,
        subtitle = subtitle,
        binary = binary,
        docUrl = docUrl,
        iconRes = iconRes,
        installScript = """
set -e
$EnsureNode
if command -v $binary >/dev/null 2>&1; then
  echo "==> $binary is already installed"
else
  echo "==> npm install -g $pkg"
  npm install -g $pkg
fi
echo "==> ready: $(command -v $binary || echo NOT_FOUND)"
"""
    )

    val agents: List<AgentSpec> = listOf(
        npmAgent(
            id = "deepseek-harness",
            name = "DeepSeek Harness",
            subtitle = "DeepSeek agentic harness · npm @deepseek-ai/dsh",
            binary = "dsh",
            docUrl = "https://deepseek-harness.github.io/deepseek-harness/",
            iconRes = R.drawable.ic_agent_deepseek,
            pkg = "@deepseek-ai/dsh"
        ),
        npmAgent(
            id = "claude-code",
            name = "Claude Code",
            subtitle = "Anthropic agentic CLI · npm @anthropic-ai/claude-code",
            binary = "claude",
            docUrl = "https://code.claude.com/docs/en/overview",
            iconRes = R.drawable.ic_agent_claude,
            pkg = "@anthropic-ai/claude-code"
        ),
        npmAgent(
            id = "opencode",
            name = "OpenCode",
            subtitle = "Open source terminal agent · npm opencode-ai",
            binary = "opencode",
            docUrl = "https://opencode.ai/docs",
            iconRes = R.drawable.ic_agent_opencode,
            pkg = "opencode-ai"
        ),
        npmAgent(
            id = "codex",
            name = "OpenAI Codex",
            subtitle = "OpenAI coding agent · npm @openai/codex",
            binary = "codex",
            docUrl = "https://developers.openai.com/codex/cli",
            iconRes = R.drawable.ic_agent_codex,
            pkg = "@openai/codex"
        ),
        npmAgent(
            id = "qodercli",
            name = "Qoder CLI",
            subtitle = "Alibaba Qoder agent · npm @qoder-ai/qodercli",
            binary = "qodercli",
            docUrl = "https://qoder.com/cli",
            iconRes = R.drawable.ic_agent_qoder,
            pkg = "@qoder-ai/qodercli"
        ),
        npmAgent(
            id = "copilot-cli",
            name = "GitHub Copilot CLI",
            subtitle = "GitHub Copilot agent · npm @github/copilot",
            binary = "copilot",
            docUrl = "https://docs.github.com/en/copilot/how-tos/copilot-cli",
            iconRes = R.drawable.ic_agent_copilot,
            pkg = "@github/copilot"
        ),
        AgentSpec(
            id = "hermes-agent",
            name = "Hermes Agent",
            subtitle = "Nous Research self-improving agent · curl installer",
            binary = "hermes",
            docUrl = "https://hermes-agent.nousresearch.com/docs/",
            iconRes = R.drawable.ic_agent_hermes,
            installScript = """
set -e
echo "==> installing Hermes Agent (Nous Research)"
if command -v hermes >/dev/null 2>&1; then
  echo "==> hermes is already installed"
  hermes --version || true
  exit 0
fi
if ! command -v curl >/dev/null 2>&1; then
  apt-get update -y
  apt-get install -y curl ca-certificates
fi
curl -fsSL https://hermes-agent.nousresearch.com/install.sh | bash
if ! command -v hermes >/dev/null 2>&1; then
  for candidate in "${'$'}HOME/.local/bin" "${'$'}HOME/.hermes/bin" "${'$'}HOME/bin"; do
    if [ -x "${'$'}candidate/hermes" ]; then
      ln -sf "${'$'}candidate/hermes" /usr/local/bin/hermes || true
      break
    fi
  done
fi
echo "==> ready: ${'$'}(command -v hermes || echo NOT_FOUND)"
hermes --version || true
echo "==> next: run 'hermes setup' to configure a model provider"
"""
        )
    )

    fun byId(id: String): AgentSpec? = agents.firstOrNull { it.id == id }
}