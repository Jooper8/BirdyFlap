package com.facens.birdyflap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class BirdyFlap extends ApplicationAdapter {
	SpriteBatch batch;
	Texture[] birds;
	Texture background;
	Texture pipeDown;
	Texture pipeTop;
	Texture gameOver;
	//Declarada a variável da textura da tela título.
	Texture titleScreen;

	//Declarado um array de texturas coin e a que será selecionada futuramente pelo código.
	Texture[] coin;
	Texture currentCoinVar;

	ShapeRenderer shapeRenderer;
	Circle birdCircle, coinCircle; //Declarado a colisão da moeda.
	Rectangle rectanglePipeTop;
	Rectangle rectanglePipeDown;

	float deviceWidth;
	float deviceHeight;
	float variation = 0;
	float gravity = 2;
	float starterBirdVerticalPosition;
	float positionPipeHorizontal, positionPipeVertical, positionCoinHorizontal, positionCoinVertical;
	float spaceBetweenPipes;
	Random random;
	int points = 0;
	int maxScore = 0;
	boolean pipePassed = false;
	int gameState = 0;
	float positionBirdHorizontal;
	BitmapFont textScore;
	BitmapFont textRestart;
	BitmapFont textBestScore;
	int coinVar;


	Sound soundFlying;
	Sound soundCollision;
	Sound soundScore;
	//Declaração da variável do som da moeda.
	Sound soundCoin;

	Preferences preferences;

	OrthographicCamera camera;
	Viewport viewport;
	final float VIRTUAL_WIDTH = 720;
	final float VIRTUAL_HEIGHT = 1280;
	
	@Override
	public void create () {
		startTextures();
		startObjects();
		startingRandomCoinSpot.nextInt(1200);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verifyGameState();
		validatePoints();
		drawTextures();
		detectCollisions();
	}

	private void startTextures(){
		birds = new Texture[3];
		birds[0] = new Texture("passaro1.png");
		birds[1] = new Texture("passaro2.png");
		birds[2] = new Texture("passaro3.png");

		background = new Texture("fundo.png");
		pipeDown = new Texture("cano_baixo_maior.png");
		pipeTop = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		//Relacionado a variável da tela título com a devida imagem.
		titleScreen = new Texture("titlescreen.png");
		coin = new Texture[2];
		coin[0] = new Texture("coin1.png");
		coin[1] = new Texture("coin2.png");
	}

	private void startObjects(){
		batch = new SpriteBatch();
		random = new Random();

		deviceWidth = VIRTUAL_WIDTH;
		deviceHeight = VIRTUAL_HEIGHT;
		starterBirdVerticalPosition = deviceHeight/2;
		positionPipeHorizontal = deviceWidth;
		positionCoinHorizontal = deviceWidth;
		spaceBetweenPipes = 350;

		textScore = new BitmapFont();
		textScore.setColor(Color.WHITE);
		textScore.getData().setScale(10);

		textRestart = new BitmapFont();
		textRestart.setColor(Color.GREEN);
		textRestart.getData().setScale(2);

		textBestScore = new BitmapFont();
		textBestScore.setColor(Color.RED);
		textBestScore.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		birdCircle = new Circle();
		coinCircle = new Circle();
		rectanglePipeDown = new Rectangle();
		rectanglePipeTop = new Rectangle();

		soundFlying = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		soundCollision = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		soundScore = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		//Declaração de qual som será relacionado à moeda.
		soundCoin = Gdx.audio.newSound(Gdx.files.internal("som_moedas.mp3"));

		preferences = Gdx.app.getPreferences("flappyBird");
		maxScore = preferences.getInteger("maxScore",0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	private void verifyGameState(){

		boolean touchScreen = Gdx.input.justTouched();
		if( gameState == 0){
			if( touchScreen){
				gravity = -15;
				gameState = 1;
				soundFlying.play();
			}
		}else if (gameState == 1){
			if(touchScreen){
				gravity = -15;
				soundFlying.play();
			}
			positionPipeHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			//Mantém a moeda gerada se movimentando horizontalmente.
			positionCoinHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if( positionPipeHorizontal < -pipeTop.getWidth()){
				positionPipeHorizontal = deviceWidth;
				positionPipeVertical = random.nextInt(400) - 200;
				pipePassed = false;
			}
			//Gera uma moeda em posição aleatória.
			if( positionCoinHorizontal < -coin[0].getWidth()) {
				positionCoinHorizontal = deviceWidth + random.nextInt(Math.round((deviceWidth * .5f)));
				positionCoinVertical = random.nextInt(Math.round(deviceHeight)) - 200;
			};
			if( starterBirdVerticalPosition > 0 || touchScreen)
				starterBirdVerticalPosition = starterBirdVerticalPosition - gravity;
			gravity++;
		}else if( gameState == 2){
			if (points> maxScore){
				maxScore = points;
				preferences.putInteger("maxScore", maxScore);
				preferences.flush();
			}
			positionBirdHorizontal -= Gdx.graphics.getDeltaTime()*500;
			if(touchScreen){
				gameState = 0;
				points = 0;
				gravity = 0;
				positionBirdHorizontal = 0;
				starterBirdVerticalPosition = deviceHeight/2;
				positionPipeHorizontal = deviceWidth;
			}
		}
	}

	private void detectCollisions(){
		birdCircle.set(
				50 + positionBirdHorizontal + birds[0].getWidth()/2,
				starterBirdVerticalPosition + birds[0].getHeight()/2,
				birds[0].getWidth()/2
		);
		//Detecta colisão da moeda.
		coinCircle.set(
				50 + positionCoinHorizontal + coin[0].getWidth()/2,
				positionCoinVertical + coin[0].getHeight()/2,
				coin[0].getWidth()/2
		);
		rectanglePipeDown.set(
				positionPipeHorizontal,
				deviceHeight/2 - pipeDown.getHeight() - spaceBetweenPipes / 2 + positionPipeVertical,
				pipeDown.getWidth(), pipeDown.getHeight()
		);
		rectanglePipeTop.set(
				positionPipeHorizontal, deviceHeight / 2 + spaceBetweenPipes / 2 + positionPipeVertical,
				pipeTop.getWidth(), pipeTop.getHeight()
		);

		boolean collidedPipeTop = Intersector.overlaps(birdCircle, rectanglePipeTop);
		boolean collidedPipeDown = Intersector.overlaps(birdCircle, rectanglePipeDown);
		boolean collidedCoin = Intersector.overlaps(birdCircle, coinCircle);
		boolean collidedCoinPipeTop = Intersector.overlaps(coinCircle, rectanglePipeTop);
		boolean collidedCoinPipeDown = Intersector.overlaps(coinCircle, rectanglePipeDown);

		//Caso moeda colida com o cano, move-se à direita.
		if(collidedCoinPipeTop || collidedCoinPipeDown){
			positionCoinHorizontal+=random.nextInt(150)+100;
		}

		//Adiciona pontos, tocarsom de moeda e gera nova moeda aleatóriamente.
		if(collidedCoin){
			if(currentCoinVar == coin[0]){
				points = points + 10;
				soundCoin.play();
			}
			else{
				points = points + 5;
				soundCoin.play();
			}
			if (random.nextInt(11)-1 > 10) {
				coinVar = 0;}
			else {
				coinVar = 1;
			}
			positionCoinHorizontal = -deviceHeight;
		}

		if (collidedPipeTop || collidedPipeDown){
			if (gameState == 1){
				soundCollision.play();
				gameState = 2;
			}
		}
	}

	private void drawTextures(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(background,0,0,deviceWidth, deviceHeight);
		batch.draw(birds[(int) variation],
				50 + positionBirdHorizontal, starterBirdVerticalPosition);
		batch.draw(coin[coinVar],
				50 + positionCoinHorizontal, positionCoinVertical);
		currentCoinVar = coin[coinVar];
		batch.draw(pipeDown, positionPipeHorizontal,
				deviceHeight/2 - pipeDown.getHeight() - spaceBetweenPipes/2 + positionPipeVertical);
		batch.draw(pipeTop, positionPipeHorizontal,
				deviceHeight/2 + spaceBetweenPipes/2 + positionPipeVertical);
		textScore.draw(batch, String.valueOf(points), deviceWidth/2.2f,
		deviceHeight - 110);

		//Caso na tela inicial do jogo, mostra a Logo.
		if(gameState == 0) {
			batch.draw(titleScreen, deviceWidth / 2 - titleScreen.getWidth() / 2, deviceHeight / 2 + 160);
		}

		if(gameState == 2){
			batch.draw(gameOver, deviceWidth/2 - gameOver.getWidth()/2,
					deviceHeight/2);
			textRestart.draw(batch,
					"Touch to restart!", deviceWidth/2 - 140,
					deviceHeight/2 - gameOver.getHeight()/2);
			textBestScore.draw(batch,
					"Your record is: "+ maxScore + " points",
					deviceWidth/2-140, deviceHeight/2 - gameOver.getHeight());
		}
		batch.end();
	}

	public void validatePoints(){
		if( positionPipeHorizontal < 50-birds[0].getWidth()){
			if (!pipePassed){
				points++;
				pipePassed = true;
				soundScore.play();
			}
		}

		variation += Gdx.graphics.getDeltaTime() * 10;

		if (variation > 3)
			variation = 0;
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
	
	@Override
	public void dispose () {
	}
}
