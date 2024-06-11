from manager import create_app

app = create_app()

def main():
    app.run(host="0.0.0.0", port=8080, debug=True)
    
if __name__ == "__main__":
    main()