import json
import os

from flask import Flask, request, jsonify, abort
import openai

app = Flask(__name__)

DEFAULT_MODEL = "gpt-4.1-mini"
DEFAULT_PROMPT = "请根据以下数据产出一句简短中文推荐理由。"
API_KEY = os.getenv("LLM_API_KEY") 
# BASE_URL = os.getenv("LLM_BASE_URL", "https://api-inference.modelscope.cn/v1")

if not API_KEY:
    raise RuntimeError("LLM_API_KEY is required")

openai.api_key = API_KEY
# openai.api_base = BASE_URL

@app.route("/reason", methods=["POST"])
def reason():
    payload = request.get_json(force=True)
    prompt = payload.get("promptOverride") or DEFAULT_PROMPT
    context_str = json.dumps(payload, ensure_ascii=False, indent=2)

    messages = [
        {"role": "user", "content": f"{prompt}\nContext:\n{context_str}"}
    ]
    try:
        response = openai.ChatCompletion.create(
            model=DEFAULT_MODEL,
            messages=messages,
            temperature=0.2,
        )
    except Exception as exc:
        abort(502, description=f"LLM Service Error: {exc}")

    choices = response.get("choices", [])
    if not choices:
        abort(502, description="empty response")

    content = choices[0].get("message", {}).get("content", "")
    if not content:
        abort(502, description="No content in response")
    
    print(content.strip())

    return jsonify({"reason": content.strip()})

if __name__ == "__main__":
    print("正在启动服务...")
    # print(f"API URL: {BASE_URL}")
    print(f"模型: {DEFAULT_MODEL}")
    # host='0.0.0.0' 允许外部访问，port=8000 是端口
    app.run(host='0.0.0.0', port=8000, debug=True)