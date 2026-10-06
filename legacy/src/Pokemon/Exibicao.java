package Pokemon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Exibicao {

	public static void exibePokeMoves (JSONArray pokeListaMoves) {
		for (int i = 0 ; i < pokeListaMoves.length();i++) {
			JSONObject pokeMoves;
			try {
				pokeMoves = pokeListaMoves.getJSONObject(i);
				JSONObject nomeMoves = (JSONObject) pokeMoves.get("move");
				String nomeMove = nomeMoves.get("name").toString();
				System.out.println(nomeMove + "\n");

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public static void exibePokeStats (JSONArray pokeListaStats) {
		for (int i = 0 ; i < pokeListaStats.length();i++) {
			JSONObject pokeStats;
			try {
				pokeStats = pokeListaStats.getJSONObject(i);
				JSONObject nomeStats = (JSONObject) pokeStats.get("stat");
				String nomeStatisticas = nomeStats.get("name").toString();
				System.out.println(nomeStatisticas + "\n");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		}

	}	
	public static void exibePokeTypes (JSONArray pokeListaTypes) {
		for (int i = 0 ; i < pokeListaTypes.length();i++) {
			JSONObject pokeType;
			try {
				pokeType = pokeListaTypes.getJSONObject(i);
				JSONObject nomeTypes = (JSONObject) pokeType.get("type");
				String nomeType = nomeTypes.get("name").toString();
				System.out.println(nomeType + "\n");
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		
		}

	}	
	
	public static void exibePokeAbilities (JSONArray pokeListaAbilities) {
		for (int i = 0 ; i < pokeListaAbilities.length();i++) {
			JSONObject pokeAbilities;
			
			
			try {
				pokeAbilities = pokeListaAbilities.getJSONObject(i);
				JSONObject nomeAbility = (JSONObject) pokeAbilities.get("ability");
				String nomeAbilities = nomeAbility.get("name").toString();
				System.out.println(nomeAbilities + "\n");
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		
		}

	}	
	
	
	public static void exibePokeImagem (int index) {
		GetImage pokeImage = new GetImage();
		System.out.println(pokeImage.getPokemonImage(index) + "\n");
	}
}
