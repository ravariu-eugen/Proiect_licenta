package proiect_licenta.planner.progress_bar;

public class ProgressBar {
	private final int nameWidth;
	private final int barWidth;


	public ProgressBar(int nameWidth, int barWidth) {
		this.nameWidth = nameWidth;
		this.barWidth = barWidth;
	}


	private static String truncateAndPad(String str, int len) {
		if (str.length() > len) {
			str = str.substring(0, len);
		}
		return String.format("%-" + len + "s", str);
	}

	private String getBar(int progress, int total) {
		int demarcation = Math.floorDiv(progress * barWidth, total);


		return "|%s%s|".formatted(
				"■".repeat(demarcation),
				"□".repeat(barWidth - demarcation)
		);
	}

	public String getLine(String name, int progress, int total) {
		return "%s %s %d/%d".formatted(
				truncateAndPad(name, nameWidth),
				getBar(progress, total),
				progress,
				total
		);
	}


}
