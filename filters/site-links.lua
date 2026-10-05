function Link(link)
  if quarto.doc.is_format("html") then
    local prefix = link.target:match("^([%.%/]*)README%.zh%-CN%.md$")
    local english = link.target:match("^([%.%/]*)README%.md$")
    if prefix then
      link.target = prefix .. "zh/index.html"
    elseif english then
      link.target = english .. "index.html"
    elseif link.target:match("tutorial/.*%.md$")
      or link.target:match("^%d%d%-.*%.md$")
      or link.target:match("foundations/.*%.md$")
      or link.target:match("^F%d%d%-.*%.md$")
      or link.target == "beginner-guide.md" or link.target == "新手学习路线与补齐说明.md"
      or link.target == "validation.md" or link.target == "sources.md"
      or link.target == "验证记录.md" or link.target == "技术文档与版本说明.md" then
      link.target = link.target:gsub("%.md$", ".html")
    end
  end
  return link
end

function Meta(meta)
  if quarto.doc.is_format("html") then
    local here = pandoc.path.directory(PANDOC_SCRIPT_FILE)
    quarto.doc.add_html_dependency({
      name = "language-switch",
      scripts = {"language-switch.js"}
    })
  end
  return meta
end

function CodeBlock(block)
  if quarto.doc.is_format("html") and block.classes:includes("mermaid") then
    local share = os.getenv("QUARTO_SHARE_PATH")
    local here = pandoc.path.directory(PANDOC_SCRIPT_FILE)
    quarto.doc.add_html_dependency({
      name = "jvm-mermaid",
      scripts = {
        pandoc.path.join({share, "formats", "html", "mermaid", "mermaid.min.js"}),
        "mermaid-init.js"
      }
    })
    local escaped = block.text:gsub("&", "&amp;"):gsub("<", "&lt;"):gsub(">", "&gt;")
    return pandoc.RawBlock("html", '<pre class="mermaid">' .. escaped .. '</pre>')
  end
end
